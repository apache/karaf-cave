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
package org.apache.karaf.cave.server.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.features.internal.resolver.ResolverUtil;
import org.apache.karaf.features.internal.resolver.ResourceBuilder;
import org.apache.karaf.features.internal.resolver.ResourceImpl;
import org.apache.karaf.features.internal.resolver.ResourceUtils;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.osgi.framework.BundleException;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.jar.JarFile.MANIFEST_NAME;
import static org.osgi.service.repository.ContentNamespace.CAPABILITY_URL_ATTRIBUTE;
import static org.osgi.service.repository.ContentNamespace.CONTENT_NAMESPACE;

/**
 * Default implementation of a Cave repository.
 */
public class CaveRepositoryImpl implements CaveRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryImpl.class);

    private String name;
    private String location;
    private OsgiRepository repository;

    public CaveRepositoryImpl(String name, String location, boolean scan) throws Exception {
        super();

        this.name = name;
        this.location = location;

        createRepositoryDirectory();
        if (scan) {
            scan();
        } else if (!Files.exists(getRepositoryXmlFile())) {
            try (Writer writer = Files.newBufferedWriter(getRepositoryXmlFile(), StandardCharsets.UTF_8)) {
                repository.writeRepository(writer);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public long getIncrement() {
        return repository.getIncrement();
    }

    public OsgiRepository getRepository() {
        return repository;
    }

    public Path getLocationPath() {
        return Paths.get(getLocation());
    }

    /**
     * Check if the repository folder exists and create it if not.
     */
    private void createRepositoryDirectory() throws Exception {
        LOGGER.debug("Create Cave repository {} folder.", getName());
        if (!Files.exists(getLocationPath())) {
            Files.createDirectories(getLocationPath());
            LOGGER.debug("Cave repository {} location has been created.", getName());
            LOGGER.debug(getLocationPath().toAbsolutePath().toString());
        }
        repository = new OsgiRepository(getRepositoryXmlFile().toUri().toString(), getName());
    }

    /**
     * Upload an artifact from the given URL.
     *
     * @param url the URL of the artifact.
     * @throws Exception in case of upload failure.
     */
    public void upload(URL url) throws Exception {
        LOGGER.debug("Upload new artifact from {}", url);
        // TODO: this is problematic if receiving multiple requests at the same time
        String artifactName = "artifact-" + System.currentTimeMillis();
        Path temp = getLocationPath().resolve(artifactName);
        try (InputStream is = url.openStream()) {
            Files.copy(is, temp);
        }
        // update the repository.xml
        ResourceImpl resource = createResource(temp.toUri().toURL());
        Path destination = getLocationPath().resolve(ResolverUtil.getSymbolicName(resource) + "-" + ResolverUtil.getVersion(resource) + ".jar");
        if (Files.exists(destination)) {
            Files.delete(temp);
            LOGGER.warn("The {} artifact is already present in the Cave repository", url);
            throw new IllegalArgumentException("The " + url.toString() + " artifact is already present in the Cave repository");
        }
        Files.move(temp, destination);
        resource = createResource(destination.toUri().toURL());
        addResources(Collections.<Resource>singletonList(resource));
    }

    /**
     * Scan the content of the whole repository to update the repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    public void scan() throws Exception {
        repository = new OsgiRepository(getRepositoryXml().toString(), getName());
        List<Resource> resources = new ArrayList<>();
        scan(new File(getLocation()), resources);
        addResources(resources);
    }

    private void addResources(List<Resource> resources) throws IOException, XMLStreamException {
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

    /**
     * Recursive method to traverse all files in the repository.
     *
     * @param entry the
     * @throws Exception
     */
    private void scan(File entry, List<Resource> resources) throws Exception {
        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            if (children != null) {
                for (File child : children) {
                    scan(child, resources);
                }
            }
        } else {
            // populate the repository
            try {
                URL bundleUrl = entry.toURI().toURL();
                if (isPotentialBundle(bundleUrl.toString())) {
                    ResourceImpl resource = createResource(bundleUrl);
                    resources.add(resource);
                }
            } catch (BundleException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Convenience method to filter Maven files with common non-bundle extensions.
     *
     * @param bundleUrl the file URL to check.
     * @return true if the file is a potential bundle, false else.
     */
    private boolean isPotentialBundle(String bundleUrl) {
        return !bundleUrl.matches(".*\\.sha1") && !bundleUrl.matches(".*\\.pom")
                && !bundleUrl.matches(".*\\.xml") && !bundleUrl.matches(".*\\.repositories")
                && !bundleUrl.matches(".*\\.properties") && !bundleUrl.matches(".*\\.lastUpdated");
    }

    /**
     * Proxy an URL (by adding repository.xml repository metadata) in the Cave repository.
     *
     * @param url    the URL to proxyFilesystem. the URL to proxyFilesystem.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @throws Exception
     */
    public void proxy(URL url, String filter) throws Exception {
        if (url.getProtocol().equals("file")) {
            // filesystem proxyFilesystem (to another folder)
            File proxyFolder = new File(url.toURI());
            List<Resource> resources = new ArrayList<>();
            proxyFilesystem(proxyFolder, filter, resources);
            addResources(resources);
        } else if (url.getProtocol().equals("http")) {
            // HTTP proxyFilesystem
            List<Resource> resources = new ArrayList<>();
            proxyHttp(url.toExternalForm(), filter, resources);
            addResources(resources);
        }
    }

    /**
     * Proxy an URL (by adding repository.xml repository metadata) in the Cave repository.
     *
     * @param url the URL to proxy.
     * @throws Exception
     */
    public void proxy(URL url) throws Exception {
        proxy(url, null);
    }

    /**
     * Proxy a local filesystem (folder).
     *
     * @param entry  the filesystem to proxyFilesystem.
     * @param filter regex filter. Only the artifacts URL matching the filter will be considered.
     * @throws Exception in case of proxyFilesystem failure
     */
    private void proxyFilesystem(File entry, String filter, List<Resource> resources) throws Exception {
        LOGGER.debug("Proxying filesystem {}", entry.getAbsolutePath());
        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            if (children != null) {
                for (File child : children) {
                    proxyFilesystem(child, filter, resources);
                }
            }
        } else {
            try {
                if ((filter == null) || (entry.toURI().toURL().toString().matches(filter))) {
                    Resource resource = createResource(entry.toURI().toURL());
                    resources.add(resource);
                }
            } catch (BundleException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private ResourceImpl createResource(URL url) throws BundleException, IOException {
        return createResource(url, url.toExternalForm());
    }

    private ResourceImpl createResource(URL url, String uri) throws BundleException, IOException {
        Map<String, String> headers = getHeaders(url);
        try {
            ResourceImpl resource = ResourceBuilder.build(uri, headers);
            useResourceRelativeUri(resource);
            return resource;
        } catch (BundleException e) {
            throw new BundleException("Unable to create resource from " + uri + ": " + e.getMessage(), e);
        }
    }

    Map<String, String> getHeaders(URL url) throws IOException, BundleException {
        try (InputStream is = url.openStream()) {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (MANIFEST_NAME.equals(entry.getName())) {
                    Attributes attributes = new Manifest(zis).getMainAttributes();
                    Map<String, String> headers = new HashMap<>();
                    for (Map.Entry attr : attributes.entrySet()) {
                        headers.put(attr.getKey().toString(), attr.getValue().toString());
                    }
                    return headers;
                }
            }
        }
        throw new BundleException("Resource " + url + " does not contain a manifest");
    }

    /**
     * Proxy a HTTP URL locally.
     *
     * @param url    the HTTP URL to proxy.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @throws Exception in case of proxy failure.
     */
    private void proxyHttp(String url, String filter, List<Resource> resources) throws Exception {
        LOGGER.debug("Proxying HTTP URL {}", url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try (InputStream is = conn.getInputStream()) {
            String type = conn.getContentType();
            if ("application/java-archive".equals(type) || "application/octet-stream".equals(type)) {
                // I have a jar/binary, potentially a resource
                try {
                    if ((filter == null) || (url.matches(filter))) {
                        Resource resource = createResource(new URL(url));
                        resources.add(resource);
                    }
                } catch (BundleException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                // try to find link to "browse"
                try {
                    Document document = Jsoup.connect(url).get();
                    for (Element link : document.select("a")) {
                        String absoluteHref = link.attr("abs:href");
                        if (absoluteHref.startsWith(url)) {
                            proxyHttp(absoluteHref, filter, resources);
                        }
                    }
                } catch (UnsupportedMimeTypeException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Populate an URL into the Cave repository, and eventually update the repository metadata.
     *
     * @param url    the URL to copy.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @param update if true the repository metadata is updated, false else.
     * @throws Exception in case of populate failure.
     */
    public void populate(URL url, String filter, boolean update) throws Exception {
        if (url.getProtocol().equals("file")) {
            // populate the Cave repository from a filesystem folder
            File populateFolder = new File(url.toURI());
            List<Resource> resources = new ArrayList<>();
            populateFromFilesystem(populateFolder, filter, update, resources);
            addResources(resources);
        }
        if (url.getProtocol().equals("http")) {
            // populate the Cave repository from a HTTP URL
            List<Resource> resources = new ArrayList<>();
            populateFromHttp(url.toExternalForm(), filter, update, resources);
            addResources(resources);
        }
    }

    /**
     * Populate an URL into the Cave repository, and eventually update the repository metadata.
     *
     * @param url    the URL to copy.
     * @param update if true the repository metadata is updated, false else.
     * @throws Exception
     */
    public void populate(URL url, boolean update) throws Exception {
        populate(url, null, update);
    }

    /**
     * Populate the Cave repository using a filesystem directory.
     *
     * @param filesystem the "source" directory.
     * @param filter     regex filter. Only artifacts URL matching the filter will be considered.
     * @param update     if true, the resources are added into the repository metadata, false else.
     * @throws Exception in case of populate failure.
     */
    private void populateFromFilesystem(File filesystem, String filter, boolean update, List<Resource> resources) throws Exception {
        LOGGER.debug("Populating from filesystem {}", filesystem.getAbsolutePath());
        if (filesystem.isDirectory()) {
            File[] children = filesystem.listFiles();
            if (children != null) {
                for (File child : children) {
                    populateFromFilesystem(child, filter, update, resources);
                }
            }
        } else {
            try {
                if ((filter == null) || (filesystem.toURI().toURL().toString().matches(filter))) {
                    ResourceImpl resource = createResource(filesystem.toURI().toURL());
                    // copy the resource
                    Path destination = getLocationPath().resolve(filesystem.getName());
                    LOGGER.debug("Copy from {} to {}", filesystem.getAbsolutePath(), destination.toAbsolutePath().toString());
                    Files.copy(filesystem.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    if (update) {
                        resource = createResource(destination.toUri().toURL());
                        LOGGER.debug("Update the repository metadata with {}-{}", ResolverUtil.getSymbolicName(resource), ResolverUtil.getVersion(resource));
                        resources.add(resource);
                    }
                }
            } catch (BundleException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Populate the Cave repository using the given URL.
     *
     * @param url    the "source" HTTP URL.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @param update true if the repository metadata should be updated, false else.
     * @throws Exception in case of populate failure.
     */
    private void populateFromHttp(String url, String filter, boolean update, List<Resource> resources) throws Exception {
        LOGGER.debug("Populating from HTTP URL {}", url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try (InputStream is = conn.getInputStream()) {
            String type = conn.getContentType();
            if ("application/java-archive".equals(type) || "application/octet-stream".equals(type)) {
                try {
                    if ((filter == null) || (url.matches(filter))) {
                        ResourceImpl resource = createResource(new URL(url));
                        LOGGER.debug("Copy {} into the Cave repository storage", url);
                        int index = url.lastIndexOf("/");
                        if (index > 0) {
                            url = url.substring(index + 1);
                        }
                        Path destination = getLocationPath().resolve(url);
                        Files.copy(is, destination);
                        if (update) {
                            resource = createResource(destination.toUri().toURL());
                            LOGGER.debug("Update repository metadata with {}-{}", ResolverUtil.getSymbolicName(resource), ResolverUtil.getVersion(resource));
                            resources.add(resource);
                        }
                    }
                } catch (BundleException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                // try to find link to "browse"
                Document document = Jsoup.parse(is, "UTF-8", url);
                for (Element link : document.select("a")) {
                    String absoluteHref = link.attr("abs:href");
                    if (absoluteHref.startsWith(url)) {
                        populateFromHttp(absoluteHref, filter, update, resources);
                    }
                }
            }
        }
    }

    /**
     * Convert the Resource absolute URI to an URI relative to the repository one.
     *
     * @param resource the Resource to manipulate.
     */
    private void useResourceRelativeUri(ResourceImpl resource) {
        for (Capability cap : resource.getCapabilities(null)) {
            if (cap.getNamespace().equals(CONTENT_NAMESPACE)) {
                String resourceURI = cap.getAttributes().get(CAPABILITY_URL_ATTRIBUTE).toString();
                String locationURI = "file:" + getLocation();
                LOGGER.debug("Converting resource URI {} relatively to repository URI {}", resourceURI, locationURI);
                if (resourceURI.startsWith(locationURI)) {
                    resourceURI = resourceURI.substring(locationURI.length() + 1);
                    LOGGER.debug("Resource URI converted to " + resourceURI);
                    // This is a bit hacky, but the map is not read only
                    cap.getAttributes().put(CAPABILITY_URL_ATTRIBUTE, resourceURI);
                }
                break;
            }
        }
    }

    /**
     * Get the File object of the repository.xml file.
     *
     * @return the File corresponding to the repository.xml.
     * @throws Exception
     */
    private Path getRepositoryXmlFile() {
        return getLocationPath().resolve("repository.xml");
    }

    public URL getResourceByUri(String uri) {
        try {
            for (Resource resource : repository.getResources()) {
                for (Capability cap : resource.getCapabilities(null)) {
                    if (CONTENT_NAMESPACE.equals(cap.getNamespace())) {
                        Object url = cap.getAttributes().get(CAPABILITY_URL_ATTRIBUTE);
                        if (uri.equals(url)) {
                            return new File(getLocation(), uri).toURI().toURL();
                        }
                    }
                }
            }
            return null;
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Return the repository.xml corresponding to this Cave repository.
     *
     * @return the URL of the repository.xml.
     * @throws Exception in case of lookup failure.
     */
    public URL getRepositoryXml() throws Exception {
        return getRepositoryXmlFile().toUri().toURL();
    }

    /**
     * Delete the repository storage folder.
     *
     * @throws Exception in case of destroy failure.
     */
    public void cleanup() throws Exception {
        Utils.deleteRecursive(getLocationPath());
    }

}
