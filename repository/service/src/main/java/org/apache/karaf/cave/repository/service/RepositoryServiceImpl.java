/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cave.repository.service;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;
import org.apache.karaf.cave.repository.service.bundlerepository.BundleRepository;
import org.apache.karaf.cave.repository.service.bundlerepository.ResourceBuilder;
import org.apache.karaf.cave.repository.service.bundlerepository.ResourceImpl;
import org.apache.karaf.cave.repository.service.bundlerepository.ResourceUtils;
import org.apache.karaf.cave.repository.service.maven.ConsoleRepositoryListener;
import org.apache.karaf.cave.repository.service.maven.ConsoleTransferListener;
import org.apache.karaf.cave.repository.service.maven.MavenServlet;
import org.apache.karaf.cave.repository.service.scheduler.RepositoryJob;
import org.apache.karaf.scheduler.ScheduleOptions;
import org.apache.karaf.scheduler.Scheduler;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.osgi.framework.BundleException;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.util.jar.JarFile.MANIFEST_NAME;
import static org.osgi.service.repository.ContentNamespace.*;

@Component(
        name = "org.apache.karaf.cave.repository",
        immediate = true
)
public class RepositoryServiceImpl implements RepositoryService {

    @Reference
    private HttpService httpService;

    @Reference
    private Scheduler scheduler;

    private final static Pattern mvnPattern = Pattern.compile("mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    private static final String STORAGE_FILE = "repositories.db";

    private File baseStorage;
    private final Map<String, Repository> repositories = new ConcurrentHashMap<>();
    private String httpContext;

    @Activate
    public void activate(ComponentContext componentContext) throws Exception {
        activate(componentContext.getProperties());
    }

    /**
     * Only visible for testing purpose
     */
    protected void activate(Dictionary<String, Object> properties) throws Exception {
        baseStorage = new File((properties.get("storage.location") != null) ? properties.get("storage.location").toString() : System.getProperty("karaf.data") + File.separator + "cave" + File.separator + "repository");
        httpContext = (properties.get("http.context") != null) ? properties.get("http.context").toString() : "/cave/repository";
        // load repositories db to populate the map and register the servlet
        load();
        for (Repository repository : repositories.values()) {
            registerMavenServlet(repository);
        }
    }

    @Deactivate
    public void deactivate(ComponentContext componentContext) throws Exception {
        Dictionary<String, Object> properties = componentContext.getProperties();
        // unregister repository servlets
        for (Repository repository : repositories.values()) {
            unregisterMavenServlet(repository);
        }
    }

    @Override
    public Repository create(String name) throws Exception {
        return create(name, null);
    }

    @Override
    public Repository create(String name, String location) throws Exception {
        return create(name, location, httpContext + "/" + name, null, false, "karaf", null, null, null, null, 8);
    }

    @Override
    public Repository create(String name, String location, String proxy) throws Exception {
        return create(name, location, httpContext + "/" + name, proxy, false, "karaf", null, null, null, null,8);
    }

    @Override
    public Repository create(String name, String location, String proxy, boolean mirror) throws Exception {
        return create(name, location, httpContext + "/" + name, proxy, mirror, "karaf", null, null, null, null, 8);
    }

    @Override
    public Repository create(String name, String location, String url, String proxy, boolean mirror, String realm, String downloadRole, String uploadRole, String scheduling, String schedulingAction, int poolSize) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Repository name is mandatory");
        }
        if (repositories.get(name) != null) {
            throw new IllegalArgumentException("Repository " + name + " already exists");
        }
        if (url == null || url.isEmpty()) {
            url = httpContext + "/" + name;
        }
        if (location == null || location.isEmpty()) {
            location = new File(baseStorage, name).getAbsolutePath();
        }
        // create the repository storage
        if (location != null && !location.isEmpty() && !Files.exists(Paths.get(location))) {
            Files.createDirectories(Paths.get(location));
        }
        // create the repository model
        Repository repository = new Repository();
        repository.setName(name);
        repository.setLocation(location);
        repository.setUrl(url);
        repository.setProxy(proxy);
        repository.setMirror(mirror);
        repository.setRealm(realm);
        repository.setDownloadRole(downloadRole);
        repository.setUploadRole(uploadRole);
        repository.setPoolSize(poolSize);
        repository.setScheduling(scheduling);
        repository.setSchedulingAction(schedulingAction);
        repositories.put(name, repository);
        // register the repository servlet
        registerMavenServlet(repository);
        // optionally register the repository scheduling job
        scheduleRepository(repository);
        // update repositories DB
        save();
        return repository;
    }

    @Override
    public void changeLocation(String name, String location) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        Repository repository = repositories.get(name);
        if (repository.getLocation() != null && !repository.getLocation().isEmpty()) {
            if (!Files.exists(Paths.get(location))) {
                Files.createDirectories(Paths.get(location));
            }
            final Path source = Paths.get(repository.getLocation());
            final Path target = Paths.get(location);
            Files.move(source, target, ATOMIC_MOVE);
        }
        if (location != null && !location.isEmpty()) {
            File locationFile = new File(location);
            repository.setLocation(locationFile.getAbsolutePath());
        } else {
            repository.setLocation(location);
        }
        repositories.put(name, repository);
        save();
    }

    @Override
    public void changeUrl(String name, String url) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        if (url == null) {
            throw new IllegalArgumentException("URL can't be null");
        }
        Repository repository = repositories.get(name);
        unregisterMavenServlet(repository);
        repository.setUrl(url);
        registerMavenServlet(repository);
        repositories.put(name, repository);
        save();
    }

    @Override
    public void changeProxy(String name, String proxy, boolean mirror) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        Repository repository = repositories.get(name);
        unregisterMavenServlet(repository);
        repository.setProxy(proxy);
        repository.setMirror(mirror);
        registerMavenServlet(repository);
        repositories.put(name, repository);
        save();
    }

    @Override
    public void changeSecurity(String name, String realm, String downloadRole, String uploadRole) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        Repository repository = repositories.get(name);
        unregisterMavenServlet(repository);
        repository.setRealm(realm);
        repository.setDownloadRole(downloadRole);
        repository.setUploadRole(uploadRole);
        registerMavenServlet(repository);
        repositories.put(name, repository);
        save();
    }

    @Override
    public void changeScheduling(String name, String scheduling, String schedulingAction) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        Repository repository = repositories.get(name);
        unscheduleRepository(repository);
        repository.setScheduling(scheduling);
        repository.setSchedulingAction(schedulingAction);
        scheduleRepository(repository);
        repositories.put(name, repository);
        save();
    }

    @Override
    public void copy(String sourceRepositoryName, String destinationRepositoryName) throws Exception {
        if (repositories.get(sourceRepositoryName) == null) {
            throw new IllegalArgumentException("Repository " + sourceRepositoryName + " doesn't exist");
        }
        if (repositories.get(destinationRepositoryName) == null) {
            throw new IllegalArgumentException("Repository " + destinationRepositoryName + " doesn't exist");
        }
        Repository sourceRepository = repositories.get(sourceRepositoryName);
        Repository destinationRepository = repositories.get(destinationRepositoryName);
        if (sourceRepository.getLocation() == null || sourceRepository.getLocation().isEmpty()) {
            throw new IllegalStateException("Source repository " + sourceRepositoryName + " location is not defined");
        }
        if (destinationRepository.getLocation() == null || destinationRepository.getLocation().isEmpty()) {
            throw new IllegalStateException("Destination repository " + destinationRepositoryName + " location is not defined");
        }
        final Path source = Paths.get(sourceRepository.getLocation());
        final Path target = Paths.get(destinationRepository.getLocation());
        Files.walkFileTree(source, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path newDir = target.resolve(source.relativize(dir));
                try {
                    Files.copy(dir, newDir, StandardCopyOption.COPY_ATTRIBUTES);
                } catch (FileAlreadyExistsException faee) {
                    // ignore
                } catch (IOException ioe) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Path newDir = target.resolve(source.relativize(dir));
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newDir, time);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void remove(String name) throws Exception {
        remove(name, false);
    }

    @Override
    public void remove(String name, boolean storageCleanup) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        Repository repository = repositories.get(name);
        // cleanup storage
        if (storageCleanup && repository.getLocation() != null && !repository.getLocation().isEmpty()) {
            purge(repository);
        }
        // unregister repository servlet
        unregisterMavenServlet(repository);
        // unschedule
        unscheduleRepository(repository);
        // remove the repository from the map and update repositories DB
        repositories.remove(name);
        save();
    }

    @Override
    public void purge(String name) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        purge(repositories.get(name));
    }

    @Override
    public synchronized Collection<Repository> repositories() {
        return repositories.values();
    }

    @Override
    public synchronized Repository repository(String name) {
        return repositories.get(name);
    }

    @Override
    public void addArtifact(String artifactUrl, String name) throws Exception {
        Map<String, String> mavenCoordinates = new HashMap<>();
        if (isMavenUrl(artifactUrl)) {
            mavenCoordinates = parseMvnUrl(artifactUrl);
        } else {
            int index = artifactUrl.lastIndexOf('.');
            if (index != -1) {
                mavenCoordinates.put("extension", artifactUrl.substring(index + 1));
                int slashIndex = artifactUrl.lastIndexOf('/');
                if (slashIndex != -1) {
                    mavenCoordinates.put("artifactId", artifactUrl.substring(slashIndex + 1, index));
                } else {
                    throw new IllegalArgumentException("Can't find possible artifactId in the provided artifact URL");
                }
            } else {
                mavenCoordinates.put("extension", "jar");
                int slashIndex = artifactUrl.lastIndexOf('/');
                if (slashIndex != -1) {
                    mavenCoordinates.put("artifactId", artifactUrl.substring(slashIndex + 1));
                } else {
                    throw new IllegalArgumentException("Can't find possible artifactId in the provided artifact URL");
                }
            }
        }
        addArtifact(artifactUrl, mavenCoordinates.get("groupId"), mavenCoordinates.get("artifactId"), mavenCoordinates.get("version"), mavenCoordinates.get("extension"), mavenCoordinates.get("classifier"), name);
    }

    @Override
    public void addArtifact(String artifactUrl, String groupId, String artifactId, String version, String type, String classifier, String name) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        if (artifactUrl == null) {
            throw new IllegalArgumentException("Artifact URL can't be null");
        }

        if (repositories.get(name).getLocation() == null || repositories.get(name).getLocation().isEmpty()) {
            throw new IllegalStateException("Repository " + name + " location is not defined");
        }

        File artifactFile = File.createTempFile(artifactId, type);
        try (FileOutputStream os = new FileOutputStream(artifactFile)) {
            copyStream(new URI(artifactUrl).toURL().openStream(), os);
            os.flush();
        }

        DefaultServiceLocator defaultServiceLocator = MavenRepositorySystemUtils.newServiceLocator();
        defaultServiceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        RepositorySystem repositorySystem = defaultServiceLocator.getService(RepositorySystem.class);
        DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(repositories.get(name).getLocation());
        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(repositorySystemSession, localRepository);
        repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        repositorySystemSession.setTransferListener(new ConsoleTransferListener(System.out));
        repositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener(System.out));
        Artifact artifact;
        if (classifier != null) {
            artifact = new DefaultArtifact(groupId, artifactId, classifier, type, version);
        } else {
            artifact = new DefaultArtifact(groupId, artifactId, type, version);
        }
        artifact = artifact.setFile(artifactFile);

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact(artifact);
        repositorySystem.install(repositorySystemSession, installRequest);
    }

    /**
     * Check if an URL is a mvn one or not.
     * <p>
     * Visible for testing purpose.
     *
     * @param url the URL to check.
     * @return true if the URL is a mvn URL, false else.
     */
    protected static boolean isMavenUrl(String url) {
        Matcher matcher = mvnPattern.matcher(url);
        return matcher.matches();
    }

    /**
     * Extract Maven coordinates from a given URL.
     * <p>
     * Visible for testing purpose.
     *
     * @param artifactUrl the artifact URL.
     * @return the extracted Maven coordinates.
     */
    protected static Map<String, String> parseMvnUrl(String artifactUrl) {
        Matcher matcher = mvnPattern.matcher(artifactUrl);
        if (!matcher.matches()) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        result.put("groupId", matcher.group(1));
        result.put("artifactId", matcher.group(2));
        result.put("version", matcher.group(3));
        if (matcher.group(5) == null) {
            result.put("extension", "jar");
        } else {
            result.put("extension", matcher.group(5));
        }
        result.put("classifier", matcher.group(7));
        return result;
    }

    static long copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            os.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    @Override
    public void deleteArtifact(String artifactUrl, String name) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        if (repositories.get(name).getLocation() == null || repositories.get(name).getLocation().isEmpty()) {
            throw new IllegalStateException("Repository " + name + " location is not defined");
        }
        Path path;
        // if the URL is a mvn URL
        if (artifactUrl.startsWith("mvn:")) {
            path = Paths.get(repositories.get(name).getLocation()).resolve(Paths.get(convertMvnUrlToPath(artifactUrl)));
        } else {
            // the artifact location is relative to the repository storage
            path = Paths.get(repositories.get(name).getLocation() + "/" + artifactUrl);
        }
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(path);
            }
        }
    }

    /**
     * Visible for testing.
     */
    protected static String convertMvnUrlToPath(String mvnUrl) {
        Map<String, String> coordinates = parseMvnUrl(mvnUrl);
        return convertMvnCoordinatesToPath(coordinates);
    }

    /**
     * Visible for testing.
     */
    protected static String convertMvnCoordinatesToPath(Map<String, String> coordinates) {
        StringBuilder builder = new StringBuilder();
        if (coordinates.get("groupId") != null) {
            builder.append(coordinates.get("groupId").replace(".", "/")).append("/");
        }
        builder.append(coordinates.get("artifactId")).append("/");
        builder.append(coordinates.get("version")).append("/");
        builder.append(coordinates.get("artifactId")).append("-").append(coordinates.get("version"));
        if (coordinates.get("classifier") != null) {
            builder.append("-").append(coordinates.get("classifier"));
        }
        builder.append(".").append(coordinates.get("extension"));
        return builder.toString();
    }

    @Override
    public void deleteArtifact(String groupId, String artifactId, String version, String type, String classifier, String name) throws Exception {
        Map<String, String> coordinates = new HashMap<>();
        coordinates.put("groupId", groupId);
        coordinates.put("artifactId", artifactId);
        coordinates.put("version", version);
        if (type == null) {
            coordinates.put("extension", "jar");
        } else {
            coordinates.put("extension", type);
        }
        coordinates.put("classifier", classifier);
        if (repositories.get(name).getLocation() != null) {
            Path path = Paths.get(repositories.get(name).getLocation()).resolve(Paths.get(convertMvnCoordinatesToPath(coordinates)));
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.delete(path);
                }
            }
        }
    }

    @Override
    public void updateBundleRepositoryDescriptor(String name) throws Exception {
        if (repositories.get(name) == null) {
            throw new IllegalArgumentException("Repository " + name + " doesn't exist");
        }
        if (repositories.get(name).getLocation() != null && !repositories.get(name).getLocation().isEmpty()) {
            Path bundleRepositoryXmlPath = Paths.get(repositories.get(name).getLocation()).resolve("repository.xml");
            BundleRepository bundleRepository = new BundleRepository(bundleRepositoryXmlPath.toUri().toString(), name);
            if (!Files.exists(bundleRepositoryXmlPath)) {
                // init the repository XML
                try (Writer writer = Files.newBufferedWriter(bundleRepositoryXmlPath, StandardCharsets.UTF_8)) {
                    bundleRepository.writeRepository(writer);
                }
            }
            List<Resource> resources = new ArrayList<>();
            updateBundleRepositoryDescriptor(new File(repositories.get(name).getLocation()), resources, repositories.get(name).getLocation());
            addResources(bundleRepository, resources);
        }
    }

    private void addResources(BundleRepository repository, List<Resource> resources) throws IOException, XMLStreamException {
        if (!resources.isEmpty()) {
            Map<String, Resource> ids = new HashMap<>();
            for (Resource resource : repository.getResources()) {
                ids.put(ResourceUtils.getUri(resource), resource);
            }
            List<Resource> toAdd = new ArrayList<>();
            for (Resource resource : resources) {
                String uri = ResourceUtils.getUri(resource);
                if (!ids.containsKey(uri)) {
                    toAdd.add(resource);
                    ids.put(uri, resource);
                }
            }
            if (!toAdd.isEmpty()) {
                repository.addResourcesAndSave(resources);
            }
        }
    }

    private void updateBundleRepositoryDescriptor(File entry, List<Resource> resources, String location) throws Exception {
        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            if (children != null) {
                for (File child : children) {
                    updateBundleRepositoryDescriptor(child, resources, location);
                }
            }
        } else {
            try {
                URL bundleUrl = entry.toURI().toURL();
                if (isBundle(bundleUrl.toString())) {
                    ResourceImpl resource = createResource(bundleUrl, location);
                    resources.add(resource);
                }
            } catch (BundleException be) {
                // nothing to do
            }
        }
    }

    private boolean isBundle(String bundleUrl) {
        return !bundleUrl.matches(".*\\.sha1") && !bundleUrl.matches(".*\\.pom")
                && !bundleUrl.matches(".*\\.xml") && !bundleUrl.matches(".*\\.repositories")
                && !bundleUrl.matches(".*\\.properties") && !bundleUrl.matches(".*\\.lastUpdated");
    }

    private ResourceImpl createResource(URL url, String location) throws BundleException, IOException, NoSuchAlgorithmException {
        return createResource(url.openConnection(), location);
    }

    private ResourceImpl createResource(URLConnection urlConnection, String location) throws BundleException, IOException, NoSuchAlgorithmException {
        return createResource(urlConnection, urlConnection.getURL().toExternalForm(), location, true);
    }

    private ResourceImpl createResource(URLConnection urlConnection, String uri, String location, boolean readFully) throws BundleException, IOException, NoSuchAlgorithmException {
        Map<String, String> headers = null;
        String digest = null;
        long size = -1;
        // find headers, estimate length and checksum
        try (ContentInputStream is = new ContentInputStream(urlConnection.getInputStream())) {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (MANIFEST_NAME.equals(entry.getName())) {
                    Attributes attributes = new Manifest(zis).getMainAttributes();
                    headers = new HashMap<>();
                    for (Map.Entry attr : attributes.entrySet()) {
                        headers.put(attr.getKey().toString(), attr.getValue().toString());
                    }
                    if (!readFully) {
                        break;
                    }
                }
            }
            if (readFully) {
                digest = is.getDigest();
                size = is.getSize();
            }
        }
        if (headers == null) {
            throw new BundleException("Resource " + urlConnection.getURL() + " does not contain a manifest");
        }
        // fix the content directive
        try {
            ResourceImpl resource = ResourceBuilder.build(uri, headers);
            for (Capability cap : resource.getCapabilities(null)) {
                if (cap.getNamespace().equals(CONTENT_NAMESPACE)) {
                    String resourceURI = cap.getAttributes().get(CAPABILITY_URL_ATTRIBUTE).toString();
                    String locationURI = "file:" + location;
                    if (resourceURI.startsWith(locationURI)) {
                        resourceURI = resourceURI.substring(locationURI.length() + 1);
                        cap.getAttributes().put(CAPABILITY_URL_ATTRIBUTE, resourceURI);
                    }
                    if (readFully) {
                        cap.getAttributes().put(CONTENT_NAMESPACE, digest);
                        cap.getAttributes().put(CAPABILITY_SIZE_ATTRIBUTE, size);
                    }
                    cap.getAttributes().put(CAPABILITY_MIME_ATTRIBUTE, "application/vnd.osgi.bundle");
                    break;
                }
            }
            return resource;
        } catch (BundleException e) {
            throw new BundleException("Unable to create resource from " + uri + ": " + e.getMessage(), e);
        }
    }

    private static class ContentInputStream extends FilterInputStream {
        final MessageDigest md;
        long size = 0;

        public ContentInputStream(InputStream is) throws NoSuchAlgorithmException {
            super(is);
            md = MessageDigest.getInstance("SHA-256");
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                md.update((byte) b);
                size++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int length = super.read(b, off, len);
            if (length > 0) {
                md.update(b, off, length);
                this.size += length;
            }
            return length;
        }

        public String getDigest() {
            byte[] digest = md.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }

        public long getSize() {
            return size;
        }

    }

    /**
     * Delete (purge) a repository location.
     *
     * @param repository the {@link Repository} to purge.
     */
    private void purge(Repository repository) throws Exception {
        if (repository.getLocation() == null || repository.getLocation().isEmpty()) {
            throw new IllegalStateException("Repository " + repository.getName() + " location is not defined");
        }
        if (Files.isDirectory(Paths.get(repository.getLocation()))) {
            Files.walkFileTree(Paths.get(repository.getLocation()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ioe) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Save in the repositories DB storage.
     * <p>
     * Only visible for testing purpose.
     */
    protected synchronized void save() throws Exception {
        Properties storage = new Properties();
        storage.setProperty("count", Integer.toString(repositories.values().size()));
        int i = 0;
        for (Repository repository : repositories.values()) {
            storage.setProperty("item." + i + ".name", repository.getName());
            storage.setProperty("item." + i + ".location", (repository.getLocation() != null) ? repository.getLocation() : "");
            storage.setProperty("item." + i + ".url", repository.getUrl());
            storage.setProperty("item." + i + ".proxy", (repository.getProxy() != null) ? repository.getProxy() : "");
            storage.setProperty("item." + i + ".mirror", (repository.isMirror()) ? "true" : "false");
            storage.setProperty("item." + i + ".realm", (repository.getRealm() != null) ? repository.getRealm() : "");
            storage.setProperty("item." + i + ".downloadRole", (repository.getDownloadRole() != null) ? repository.getDownloadRole() : "");
            storage.setProperty("item." + i + ".uploadRole", (repository.getUploadRole() != null) ? repository.getUploadRole() : "");
            storage.setProperty("item." + i + ".poolSize", Integer.toString(repository.getPoolSize()));
            i++;
        }
        saveStorage(storage, new File(baseStorage, STORAGE_FILE), "Cave Repositories DB");
    }

    /**
     * Load repositories DB storage.
     * <p>
     * Only visible for testing purpose.
     */
    protected synchronized void load() throws Exception {
        File storageFile = new File(baseStorage, STORAGE_FILE);
        Properties storage = loadStorage(storageFile);
        int count = 0;
        if (storage.getProperty("count") != null) {
            count = Integer.parseInt(storage.getProperty("count"));
        }
        for (int i = 0; i < count; i++) {
            String name = storage.getProperty("item." + i + ".name");
            String location = (storage.getProperty("item." + i + ".location").isEmpty()) ? null : storage.getProperty("item." + i + ".location");
            String url = storage.getProperty("item." + i + ".url");
            String proxy = (storage.getProperty("item." + i + ".proxy").isEmpty()) ? null : storage.getProperty("item." + i + ".proxy");
            boolean mirror = Boolean.parseBoolean(storage.getProperty("item." + i + ".mirror"));
            String realm = (storage.getProperty("item." + i + ".realm").isEmpty()) ? null : storage.getProperty("item." + i + ".realm");
            String downloadRole = (storage.getProperty("item." + i + ".downloadRole").isEmpty()) ? null : storage.getProperty("item." + i + ".downloadRole");
            String uploadRole = (storage.getProperty("item." + i + ".uploadRole").isEmpty()) ? null : storage.getProperty("item." + i + ".uploadRole");
            int poolSize = Integer.parseInt(storage.getProperty("item." + i + ".poolSize"));
            Repository repository = new Repository();
            repository.setName(name);
            repository.setLocation(location);
            repository.setUrl(url);
            repository.setProxy(proxy);
            repository.setMirror(mirror);
            repository.setRealm(realm);
            repository.setDownloadRole(downloadRole);
            repository.setUploadRole(uploadRole);
            repository.setPoolSize(poolSize);
            repositories.put(name, repository);
        }
    }

    /**
     * Write the repositories DB.
     *
     * @param properties the repositories storage model.
     * @param location   the repositories DB location.
     * @param comment    a header comment in the DB file.
     */
    private void saveStorage(Properties properties, File location, String comment) throws Exception {
        if (!location.exists()) {
            location.getParentFile().mkdirs();
            location.createNewFile();
        }
        try (OutputStream outputStream = new FileOutputStream(location)) {
            properties.store(outputStream, comment);
        }
    }

    /**
     * Load the repositories DB.
     *
     * @param location the repositories DB location.
     * @return the repositories storage model.
     */
    private Properties loadStorage(File location) throws Exception {
        Properties properties = new Properties();
        if (location.exists()) {
            try (InputStream inputStream = new FileInputStream(location)) {
                properties.load(inputStream);
            }
        }
        return properties;
    }

    /**
     * Register a Maven Servlet in the HTTP Service for the given repository.
     *
     * @param repository the {@link Repository} to publish.
     */
    private void registerMavenServlet(Repository repository) throws Exception {
        Hashtable<String, String> mavenResolverConfig = new Hashtable<>();
        mavenResolverConfig.put("defaultRepositories", "file:" + repository.getLocation() + "@id=" + repository.getName() + "@snapshots@releases");
        mavenResolverConfig.put("defaultLocalRepoAsRemote", "false");
        mavenResolverConfig.put("useFallbackRepositories", "false");
        if (repository.getProxy() == null || repository.getProxy().isEmpty() || repository.isMirror()) {
            if (repository.getLocation() != null && !repository.getLocation().isEmpty()) {
                mavenResolverConfig.put("localRepository", repository.getLocation());
            }
        }
        if (repository.getProxy() != null && !repository.getProxy().isEmpty()) {
            mavenResolverConfig.put("repositories", repository.getProxy() + ",file:" + repository.getLocation() + "@id=" + repository.getName() + "@snapshots");
        } else {
            mavenResolverConfig.put("repositories", "file:" + repository.getLocation() + "@id=" + repository.getName() + "@snapshots");
        }
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(mavenResolverConfig, null);
        MavenServlet mavenServlet = new MavenServlet(mavenResolver, repository.getName(), repository.getLocation(), repository.getPoolSize(), repository.getRealm(), repository.getDownloadRole(), repository.getUploadRole());
        httpService.registerServlet(repository.getUrl(), mavenServlet, null, null);
    }

    /**
     * Register repository scheduling in the scheduler service.
     *
     * @param repository the repository to schedule.
     */
    private void scheduleRepository(Repository repository) throws Exception {
        if (repository.getScheduling() != null) {
            ScheduleOptions scheduleOptions;
            if (repository.getScheduling().contains(":")) {
                String[] schedule = repository.getScheduling().split(":");
                if (schedule[0].equalsIgnoreCase("cron")) {
                    scheduleOptions = scheduler.EXPR(schedule[1]);
                } else if (schedule[0].equalsIgnoreCase("at")) {
                    scheduleOptions = scheduler.AT(DatatypeConverter.parseDateTime(schedule[1]).getTime());
                } else {
                    throw new IllegalStateException("Unknown scheduling definition: " + repository.getScheduling());
                }
            } else {
                scheduleOptions = scheduler.EXPR(repository.getScheduling());
            }
            scheduleOptions.name("cave-repository-" + repository.getName());
            scheduler.schedule(new RepositoryJob(this, repository), scheduleOptions);
        }
    }

    private void unscheduleRepository(Repository repository) throws Exception {
        if (scheduler != null && scheduler.getJobs() != null) {
            if (scheduler.getJobs().get("cave-repository-" + repository.getName()) != null) {
                scheduler.unschedule("cave-repository-" + repository.getName());
            }
        }
    }

    /**
     * Unregister the Maven servlet for a repository.
     *
     * @param repository the {@link Repository}.
     */
    private void unregisterMavenServlet(Repository repository) {
        httpService.unregister(repository.getUrl());
    }

    /**
     * Only visible for testing purpose.
     */
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Only visible for testing purpose.
     */
    protected void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Only visible for testing purpose.
     */
    protected void clear() {
        repositories.clear();
    }
}
