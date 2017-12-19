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
package org.apache.karaf.cave.deployer.service.impl;

import com.google.common.io.Files;
import org.apache.karaf.cave.deployer.api.Connection;
import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.cave.deployer.api.FeaturesRepository;
import org.apache.karaf.features.internal.model.*;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DeployerImpl implements Deployer {

    private final static Logger LOGGER  = LoggerFactory.getLogger(DeployerImpl.class);

    private final static Pattern mvnPattern = Pattern.compile("mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    private final static String CONFIG_PID = "org.apache.karaf.cave.deployer";

    private ConfigurationAdmin configurationAdmin;

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    @Override
    public void registerConnection(Connection connection) throws Exception {
        Configuration configuration = configurationAdmin.getConfiguration(CONFIG_PID);
        Dictionary<String, Object> properties = configuration.getProperties();
        properties.put(connection.getName() + ".jmx", connection.getJmxUrl());
        properties.put(connection.getName() + ".instance", connection.getKarafName());
        properties.put(connection.getName() + ".username", connection.getUser());
        properties.put(connection.getName() + ".password", connection.getPassword());
        configuration.update(properties);
    }

    @Override
    public void deleteConnection(String connection) throws Exception {
        Configuration configuration = configurationAdmin.getConfiguration(CONFIG_PID);
        Dictionary<String, Object> properties = configuration.getProperties();
        properties.remove(connection + ".jmx");
        properties.remove(connection + ".instance");
        properties.remove(connection + ".username");
        properties.remove(connection + ".password");
        configuration.update(properties);
    }

    @Override
    public List<Connection> connections() throws Exception {
        List<Connection> connections = new ArrayList<>();

        Configuration configuration = configurationAdmin.getConfiguration(CONFIG_PID);
        Dictionary<String, Object> properties = configuration.getProperties();
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.endsWith(".jmx")) {
                String connectionName = key.substring(0, key.indexOf(".jmx"));
                Connection connection = new Connection();
                connection.setName(connectionName);
                connection.setJmxUrl((String) properties.get(connectionName + ".jmx"));
                connection.setKarafName((String) properties.get(connectionName + ".instance"));
                connection.setUser((String) properties.get(connectionName + ".username"));
                connection.setPassword((String) properties.get(connectionName + ".password"));
                connections.add(connection);
            }
        }

        return connections;
    }

    private Connection getConnection(String name) throws Exception {
        Connection connection = new Connection();
        Configuration configuration = configurationAdmin.getConfiguration(CONFIG_PID);
        Dictionary<String, Object> properties = configuration.getProperties();
        String jmx = (String) properties.get(name + ".jmx");
        String instance = (String) properties.get(name + ".instance");
        String username = (String) properties.get(name + ".username");
        String password = (String) properties.get(name + ".password");
        if (jmx == null || instance == null) {
            throw new IllegalArgumentException("No connection found with name " + name);
        }
        connection.setJmxUrl(jmx);
        connection.setKarafName(instance);
        connection.setUser(username);
        connection.setPassword(password);
        return connection;
    }

    @Override
    public void download(String artifact, String directory) throws Exception {
        InputStream is = new URI(artifact).toURL().openStream();
        File file = new File(directory);
        file.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(file);
        copyStream(is, os);
    }

    @Override
    public void explode(String artifact, String repository) throws Exception {
        File tempDirectory = Files.createTempDir();
        extract(artifact, tempDirectory);
        File karRepository = new File(tempDirectory, "repository");
        browseKar(karRepository, karRepository.getPath(), repository);
    }

    @Override
    public void extract(String artifact, String directory) throws Exception {
        File directoryFile = new File(directory);
        directoryFile.mkdirs();
        extract(artifact, directoryFile);
    }

    protected void browseKar(File entry, String basePath, String repositoryUrl) {
        if (entry.isDirectory()) {
            File[] files = entry.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    browseKar(file, basePath, repositoryUrl);
                } else {
                    String path = file.getParentFile().getParentFile().getParentFile().getPath();
                    if (path.startsWith(basePath)) {
                        path = path.substring(basePath.length() + 1);
                    }
                    path = path.replace('/', '.');
                    String groupId = path;
                    String artifactId = file.getParentFile().getParentFile().getName();
                    String version = file.getParentFile().getName();
                    String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    try {
                        uploadArtifact(groupId, artifactId, version, extension, file, repositoryUrl);
                    } catch (Exception e) {
                        LOGGER.warn("Can't upload artifact {}/{}/{}/{}", new String[]{groupId, artifactId, version, extension}, e);
                    }
                }
            }
        }
    }

    protected static boolean isMavenUrl(String url) {
        Matcher m = mvnPattern.matcher(url);
        return m.matches();
    }

    protected static Map<String, String> parse(String url) {
        Matcher m = mvnPattern.matcher(url);
        if (!m.matches()) {
            return null;
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("groupId", m.group(1));
        result.put("artifactId", m.group(2));
        result.put("version", m.group(3));
        if (m.group(5) == null) {
            result.put("extension", "jar");
        } else {
            result.put("extension", m.group(5));
        }
        result.put("classifier", m.group(7));
        return result;
    }

    public void extract(String url, File baseDir) throws Exception {
        InputStream is = null;
        JarInputStream zipIs = null;

        try {
            is = new URI(url).toURL().openStream();
            baseDir.mkdirs();

            zipIs = new JarInputStream(is);
            boolean scanForRepos = true;

            ZipEntry entry = zipIs.getNextEntry();
            while (entry != null) {
                String path = entry.getName();
                File destFile = new File(baseDir, path);
                extract(zipIs, entry, destFile);
                entry = zipIs.getNextEntry();
            }
        } finally {
            if (zipIs != null) {
                zipIs.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    private static File extract(InputStream is, ZipEntry zipEntry, File dest) throws Exception {
        if (zipEntry.isDirectory()) {
            dest.mkdirs();
        } else {
            dest.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(dest);
            copyStream(is, out);
            out.close();
        }
        return dest;
    }

    static long copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[10000];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    @Override
    public void upload(String groupId,
                       String artifactId,
                       String version,
                       String artifactUrl,
                       String repositoryUrl) throws Exception {

        Map<String, String> coordonates = new HashMap<String, String>();
        if (isMavenUrl(artifactUrl)) {
            coordonates = parse(artifactUrl);
        } else {
            int index = artifactUrl.lastIndexOf('.');
            if (index != -1) {
                coordonates.put("extension", artifactUrl.substring(index + 1));
            } else {
                coordonates.put("extension", "jar");
            }
        }

        File artifactFile = File.createTempFile(artifactId, coordonates.get("extension"));

        FileOutputStream os = new FileOutputStream(artifactFile);
        copyStream(new URI(artifactUrl).toURL().openStream(), os);
        os.flush();
        os.close();

        uploadArtifact(groupId, artifactId, version, coordonates.get("extension"), artifactFile, repositoryUrl);
    }

    protected void uploadArtifact(String groupId, String artifactId, String version, String extension, File artifactFile, String repositoryUrl) throws Exception {
        uploadArtifact(groupId, artifactId, version, extension, null, artifactFile, repositoryUrl);
    }

    protected void uploadArtifact(String groupId, String artifactId, String version, String extension, String classifier, File artifactFile, String repositoryUrl) throws Exception {
        DefaultServiceLocator defaultServiceLocator = MavenRepositorySystemUtils.newServiceLocator();
        defaultServiceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        RepositorySystem repositorySystem = defaultServiceLocator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(repositorySystemSession, localRepository);
        repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        repositorySystemSession.setTransferListener(new ConsoleTransferListener(System.out));
        repositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener(System.out));

        RemoteRepository remoteRepository = new RemoteRepository.Builder("sdeployer", "default", repositoryUrl).build();

        Artifact artifact;
        if (classifier != null) {
            artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);
        } else {
            artifact = new DefaultArtifact(groupId, artifactId, extension, version);
        }
        artifact = artifact.setFile(artifactFile);

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact(artifact);
        repositorySystem.install(repositorySystemSession, installRequest);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact(artifact);
        deployRequest.setRepository(remoteRepository);
        repositorySystem.deploy(repositorySystemSession, deployRequest);
    }

    @Override
    public void assembleFeature(String groupId,
                                String artifactId,
                                String version,
                                String repositoryUrl,
                                String feature,
                                List<String> featuresRepositoryUrls,
                                List<String> features,
                                List<String> bundles,
                                List<org.apache.karaf.cave.deployer.api.Config> configs) throws Exception {
        Features featuresModel = new Features();
        featuresModel.setName(feature);
        // add features repository
        if (featuresRepositoryUrls != null) {
            for (String featuresRepositoryUrl : featuresRepositoryUrls) {
                featuresModel.getRepository().add(featuresRepositoryUrl);
            }
        }
        // add wrap feature
        Feature wrapFeature = new Feature();
        wrapFeature.setName(feature);
        wrapFeature.setVersion(version);
        // add inner features
        if (features != null) {
            for (String innerFeature : features) {
                Dependency dependency = new Dependency();
                dependency.setName(innerFeature);
                wrapFeature.getFeature().add(dependency);
            }
        }
        // add bundles
        if (bundles != null) {
            for (String innerBundle : bundles) {
                Bundle bundle = new Bundle();
                bundle.setLocation(innerBundle);
                wrapFeature.getBundle().add(bundle);
            }
        }
        // add config
        if (configs != null) {
            for (org.apache.karaf.cave.deployer.api.Config config : configs) {
                Config modelConfig = new Config();
                modelConfig.setName(config.getPid());
                StringBuilder builder = new StringBuilder();
                if (config.getProperties() != null) {
                    for (String key : config.getProperties().keySet()) {
                        builder.append(key).append("=").append(config.getProperties().get(key)).append('\n');
                    }
                }
                modelConfig.setValue(builder.toString());
                wrapFeature.getConfig().add(modelConfig);
            }
        }
        featuresModel.getFeature().add(wrapFeature);
        File featuresFile = File.createTempFile(artifactId, "xml");
        FileOutputStream os = new FileOutputStream(featuresFile);
        JaxbUtil.marshal(featuresModel, os);
        uploadArtifact(groupId, artifactId, version, "xml", "features", featuresFile, repositoryUrl);
    }

    @Override
    public void installKar(String artifactUrl, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "install", new Object[]{ artifactUrl }, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void uninstallKar(String id, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "uninstall", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<String> kars(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + connection.getKarafName());
            return ((List<String>) mBeanServerConnection.getAttribute(name, "Kars"));
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void installBundle(String artifactUrl, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "install", new Object[]{ artifactUrl, true }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void uninstallBundle(String id, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "uninstall", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void startBundle(String id, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "start", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void stopBundle(String id, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "stop", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<org.apache.karaf.cave.deployer.api.Bundle> bundles(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "Bundles");
            List<org.apache.karaf.cave.deployer.api.Bundle> result = new ArrayList<org.apache.karaf.cave.deployer.api.Bundle>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                Long bundleId = (Long) compositeData.get("ID");
                String bundleName = (String) compositeData.get("Name");
                String bundleVersion = (String) compositeData.get("Version");
                String bundleState = (String) compositeData.get("State");
                Integer bundleStartLevel = (Integer) compositeData.get("Start Level");
                org.apache.karaf.cave.deployer.api.Bundle bundle = new org.apache.karaf.cave.deployer.api.Bundle();
                bundle.setId(bundleId.toString());
                bundle.setName(bundleName);
                bundle.setVersion(bundleVersion);
                bundle.setState(bundleState);
                bundle.setStartLevel(bundleStartLevel);
                result.add(bundle);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void addFeaturesRepository(String artifactUrl, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "addRepository", new Object[]{ artifactUrl, false }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void removeFeaturesRepository(String artifactUrl, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "removeRepository", new Object[]{ artifactUrl, true }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<FeaturesRepository> featuresRepositories(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            List<FeaturesRepository> result = new ArrayList<FeaturesRepository>();
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "Repositories");
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String repoName = (String) compositeData.get("Name");
                String repoUri = (String) compositeData.get("Uri");
                FeaturesRepository repo = new FeaturesRepository();
                repo.setName(repoName);
                repo.setUri(repoUri);
                result.add(repo);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void installFeature(String feature, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "installFeature", new Object[]{ feature }, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void uninstallFeature(String feature, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "uninstallFeature", new Object[]{ feature }, new String[]{ "java.lang.String", });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<org.apache.karaf.cave.deployer.api.Feature> features(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "Features");
            List<org.apache.karaf.cave.deployer.api.Feature> result = new ArrayList<>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String featureName = (String) compositeData.get("Name");
                String featureVersion = (String) compositeData.get("Version");
                boolean featureInstalled = (Boolean) compositeData.get("Installed");
                org.apache.karaf.cave.deployer.api.Feature feature = new org.apache.karaf.cave.deployer.api.Feature();
                feature.setName(featureName);
                feature.setVersion(featureVersion);
                if (featureInstalled)
                    feature.setState("Installed");
                else feature.setState("Uninstalled");
                result.add(feature);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<String> installedFeatures(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "Features");
            List<String> result = new ArrayList<String>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String featureName = (String) compositeData.get("Name");
                String featureVersion = (String) compositeData.get("Version");
                boolean featureInstalled = (Boolean) compositeData.get("Installed");
                if (featureInstalled)
                    result.add(featureName + "/" + featureVersion);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void createConfig(String pid, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "create", new Object[]{ pid }, new String[]{ String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void deleteConfig(String pid, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "delete", new Object[]{ pid }, new String[]{ String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void setConfigProperty(String pid, String key, String value, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "setProperty", new Object[]{ pid, key, value }, new String[]{ String.class.getName(), String.class.getName(), String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public String configProperty(String pid, String key, String connectionName) throws Exception {
        Map<String, String> properties = this.configProperties(pid, connectionName);
        return properties.get(key);
    }

    @Override
    public void deleteConfigProperty(String pid, String key, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "deleteProperty", new Object[]{ pid, key }, new String[]{ String.class.getName(), String.class.getName()} );
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void appendConfigProperty(String pid, String key, String value, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "appendProperty", new Object[]{ pid, key, value }, new String[]{ String.class.getName(), String.class.getName(), String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void updateConfig(org.apache.karaf.cave.deployer.api.Config config, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "update", new Object[] { config.getPid(), config.getProperties() }, new String[]{ String.class.getName(), Map.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public Map<String, String> configProperties(String pid, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + connection.getKarafName());
            Map<String, String> result = (Map<String, String>) mBeanServerConnection.invoke(name, "listProperties", new Object[]{ pid }, new String[]{ String.class.getName() });
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public List<String> clusterNodes(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        List<String> nodes = new ArrayList<String>();
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=node,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "nodes");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String id = (String) data.get("id");
                nodes.add(id);
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return nodes;
    }

    @Override
    public Map<String, List<String>> clusterGroups(String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=group,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "groups");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String group = (String) data.get("name");
                String members = (String) data.get("members");
                List<String> m = Arrays.asList(members.split(" "));
                groups.put(group, m);
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return groups;
    }

    @Override
    public void clusterFeatureInstall(String feature, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "installFeature", new Object[]{ clusterGroup, feature }, new String[]{ "java.lang.String", "java.lang.String"});
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public boolean isFeatureOnClusterGroup(String feature, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "features");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String featureName = (String) data.get("name");
                boolean installed = (Boolean) data.get("installed");
                if (feature.equals(featureName) && installed) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    @Override
    public boolean isFeatureLocal(String feature, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "features");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String featureName = (String) data.get("name");
                boolean installed = (Boolean) data.get("installed");
                if (feature.equals(featureName) && installed) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    @Override
    public void clusterRemoveFeaturesRepository(String id, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "removeRepository", new Object[]{ clusterGroup, id }, new String[]{ "java.lang.String", "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public boolean isFeaturesRepositoryLocal(String id, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + connection.getKarafName());
            TabularData tabularData = (TabularData) mBeanServerConnection.getAttribute(name, "repositories");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String repoName = (String) data.get("Name");
                String url = (String) data.get("Uri");
                if (repoName.equals(id) || url.equals(id)) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    @Override
    public void clusterFeatureUninstall(String feature, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "uninstallFeature", new Object[]{ clusterGroup, feature }, new String[]{ "java.lang.String", "java.lang.String"});
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public void clusterAddFeaturesRepository(String url, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            mBeanServerConnection.invoke(name, "addRepository", new Object[]{ clusterGroup, url }, new String[]{ "java.lang.String", "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    @Override
    public boolean isFeaturesRepositoryOnClusterGroup(String id, String clusterGroup, String connectionName) throws Exception {
        Connection connection = getConnection(connectionName);
        JMXConnector jmxConnector = connect(connection.getJmxUrl(),
                connection.getKarafName(),
                connection.getUser(),
                connection.getPassword());
        try {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + connection.getKarafName());
            List<String> repositories = (List<String>) mBeanServerConnection.getAttribute(name, "repositories");
            for (String repository : repositories) {
                if (repository.equals("id")) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    private JMXConnector connect(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxUrl);
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        String[] credentials = new String[]{ user, password };
        env.put("jmx.remote.credentials", credentials);
        return JMXConnectorFactory.connect(jmxServiceURL, env);
    }

}
