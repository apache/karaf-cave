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

import org.apache.commons.io.FileUtils;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.karaf.cave.server.api.CaveRepository;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

/**
 * Default implementation of a Cave repository.
 */
public class CaveRepositoryImpl extends CaveRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryImpl.class);

    private RepositoryImpl obrRepository;

    public CaveRepositoryImpl(String name, String location, boolean scan) throws Exception {
        super();

        this.setName(name);
        this.setLocation(location);

        this.createRepositoryDirectory();
        if (scan) {
            this.scan();
        }
    }

    /**
     * Check if the repository folder exists and create it if not.
     */
    private void createRepositoryDirectory() throws Exception {
        LOGGER.debug("Create Cave repository {} folder.", this.getName());
        File locationFile = new File(this.getLocation());
        if (!locationFile.exists()) {
            locationFile.mkdirs();
            LOGGER.debug("Cave repository {} location has been created.", this.getName());
            LOGGER.debug(locationFile.getAbsolutePath());
        }
        obrRepository = new RepositoryImpl();
        obrRepository.setName(this.getName());
    }

    /**
     * Generate the repository.xml with the artifact at the given URL.
     *
     * @throws Exception in case of repository.xml update failure.
     */
    private void generateRepositoryXml() throws Exception {
        File repositoryXml = this.getRepositoryXmlFile();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(repositoryXml));
        new DataModelHelperImpl().writeRepository(obrRepository, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Add a resource in the OBR repository.
     *
     * @param resource the resource to add.
     * @throws Exception in case of failure.
     */
    private void addResource(ResourceImpl resource) throws Exception {
        if (resource != null) {
            this.useResourceRelativeUri(resource);
            obrRepository.addResource(resource);
            obrRepository.setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * Upload an artifact from the given URL.
     *
     * @param url the URL of the artifact.
     * @throws Exception in case of upload failure.
     */
    public void upload(URL url) throws Exception {
        LOGGER.debug("Upload new artifact from {}", url);
        String artifactName = "artifact-" + System.currentTimeMillis();
        File temp = new File(new File(this.getLocation()), artifactName);
        FileUtils.copyURLToFile(url, temp);
        // update the repository.xml
        ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(temp.toURI().toURL());
        if (resource == null) {
            temp.delete();
            LOGGER.warn("The {} artifact source is not a valid OSGi bundle", url);
            throw new IllegalArgumentException("The " + url.toString() + " artifact source is not a valid OSGi bundle");
        }
        File destination = new File(new File(this.getLocation()), resource.getSymbolicName() + "-" + resource.getVersion() + ".jar");
        if (destination.exists()) {
            temp.delete();
            LOGGER.warn("The {} artifact is already present in the Cave repository", url);
            throw new IllegalArgumentException("The " + url.toString() + " artifact is already present in the Cave repository");
        }
        FileUtils.moveFile(temp, destination);
        resource = (ResourceImpl) new DataModelHelperImpl().createResource(destination.toURI().toURL());
        this.addResource(resource);
        this.generateRepositoryXml();
    }

    /**
     * Scan the content of the whole repository to update the repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    public void scan() throws Exception {
        obrRepository = new RepositoryImpl();
        obrRepository.setName(this.getName());
        this.scan(new File(this.getLocation()));
        this.generateRepositoryXml();
    }

    /**
     * Recursive method to traverse all files in the repository.
     *
     * @param entry the
     * @throws Exception
     */
    private void scan(File entry) throws Exception {
        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            for (int i = 0; i < children.length; i++) {
                scan(children[i]);
            }
        } else {
            // populate the repository
            try {
                URL bundleUrl = entry.toURI().toURL();
                if (isPotentialBundle(bundleUrl.toString())) {
                    ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(bundleUrl);
                    this.addResource(resource);
                }
            } catch (IllegalArgumentException e) {
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
     * Proxy an URL (by adding repository.xml OBR information) in the Cave repository.
     *
     * @param url    the URL to proxyFilesystem. the URL to proxyFilesystem.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @throws Exception
     */
    public void proxy(URL url, String filter) throws Exception {
        if (url.getProtocol().equals("file")) {
            // filesystem proxyFilesystem (to another folder)
            File proxyFolder = new File(url.toURI());
            this.proxyFilesystem(proxyFolder, filter);
        }
        if (url.getProtocol().equals("http")) {
            // HTTP proxyFilesystem
            this.proxyHttp(url.toExternalForm(), filter);
        }
        this.generateRepositoryXml();
    }

    /**
     * Proxy an URL (by adding repository.xml OBR information) in the Cave repository.
     *
     * @param url the URL to proxy.
     * @throws Exception
     */
    public void proxy(URL url) throws Exception {
        this.proxy(url, null);
    }

    /**
     * Proxy a local filesystem (folder).
     *
     * @param entry  the filesystem to proxyFilesystem.
     * @param filter regex filter. Only the artifacts URL matching the filter will be considered.
     * @throws Exception in case of proxyFilesystem failure
     */
    private void proxyFilesystem(File entry, String filter) throws Exception {
        LOGGER.debug("Proxying filesystem {}", entry.getAbsolutePath());
        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            for (int i = 0; i < children.length; i++) {
                proxyFilesystem(children[i], filter);
            }
        } else {
            try {
                if ((filter == null) || (entry.toURI().toURL().toString().matches(filter))) {
                    Resource resource = new DataModelHelperImpl().createResource(entry.toURI().toURL());
                    if (resource != null) {
                        obrRepository.addResource(resource);
                        obrRepository.setLastModified(System.currentTimeMillis());
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Proxy a HTTP URL locally.
     *
     * @param url    the HTTP URL to proxy.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @throws Exception in case of proxy failure.
     */
    private void proxyHttp(String url, String filter) throws Exception {
        LOGGER.debug("Proxying HTTP URL {}", url);
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            if (entity.getContentType().getValue().equals("application/java-archive")
                    || entity.getContentType().getValue().equals("application/octet-stream")) {
                // I have a jar/binary, potentially a resource
                try {
                    if ((filter == null) || (url.matches(filter))) {
                        Resource resource = new DataModelHelperImpl().createResource(new URL(url));
                        if (resource != null) {
                            obrRepository.addResource(resource);
                            obrRepository.setLastModified(System.currentTimeMillis());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                // try to find link to "browse"
                try {
                    Document document = Jsoup.connect(url).get();

                    Elements links = document.select("a");
                    if (links.size() > 1) {
                        for (int i = 1; i < links.size(); i++) {
                            Element link = links.get(i);
                            String absoluteHref = link.attr("abs:href");
                            this.proxyHttp(absoluteHref, filter);
                        }
                    }
                } catch (UnsupportedMimeTypeException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Populate an URL into the Cave repository, and eventually update the OBR information.
     *
     * @param url    the URL to copy.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @param update if true the OBR information is updated, false else.
     * @throws Exception in case of populate failure.
     */
    public void populate(URL url, String filter, boolean update) throws Exception {
        if (url.getProtocol().equals("file")) {
            // populate the Cave repository from a filesystem folder
            File populateFolder = new File(url.toURI());
            this.populateFromFilesystem(populateFolder, filter, update);
        }
        if (url.getProtocol().equals("http")) {
            // populate the Cave repository from a HTTP URL
            this.populateFromHttp(url.toExternalForm(), filter, update);
        }
        if (update) {
            this.generateRepositoryXml();
        }
    }

    /**
     * Populate an URL into the Cave repository, and eventually update the OBR information.
     *
     * @param url    the URL to copy.
     * @param update if true the OBR information is updated, false else.
     * @throws Exception
     */
    public void populate(URL url, boolean update) throws Exception {
        this.populate(url, null, update);
    }

    /**
     * Populate the Cave repository using a filesystem directory.
     *
     * @param filesystem the "source" directory.
     * @param filter     regex filter. Only artifacts URL matching the filter will be considered.
     * @param update     if true, the resources are added into the OBR metadata, false else.
     * @throws Exception in case of populate failure.
     */
    private void populateFromFilesystem(File filesystem, String filter, boolean update) throws Exception {
        LOGGER.debug("Populating from filesystem {}", filesystem.getAbsolutePath());
        if (filesystem.isDirectory()) {
            File[] children = filesystem.listFiles();
            for (int i = 0; i < children.length; i++) {
                populateFromFilesystem(children[i], filter, update);
            }
        } else {
            try {
                if ((filter == null) || (filesystem.toURI().toURL().toString().matches(filter))) {
                    ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(filesystem.toURI().toURL());
                    if (resource != null) {
                        // copy the resource
                        File destination = new File(new File(this.getLocation()), filesystem.getName());
                        LOGGER.debug("Copy from {} to {}", filesystem.getAbsolutePath(), destination.getAbsolutePath());
                        FileUtils.copyFile(filesystem, destination);
                        if (update) {
                            resource = (ResourceImpl) new DataModelHelperImpl().createResource(destination.toURI().toURL());
                            LOGGER.debug("Update the OBR metadata with {}", resource.getId());
                            this.addResource(resource);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Populate the Cave repository using the given URL.
     *
     * @param url    the "source" HTTP URL.
     * @param filter regex filter. Only artifacts URL matching the filter will be considered.
     * @param update true if the OBR metadata should be updated, false else.
     * @throws Exception in case of populate failure.
     */
    private void populateFromHttp(String url, String filter, boolean update) throws Exception {
        LOGGER.debug("Populating from HTTP URL {}", url);
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            if (entity.getContentType().getValue().equals("application/java-archive")
                    || entity.getContentType().getValue().equals("application/octet-stream")) {
                // I have a jar/binary, potentially a resource
                try {
                    if ((filter == null) || (url.matches(filter))) {
                        ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(new URL(url));
                        if (resource != null) {
                            LOGGER.debug("Copy {} into the Cave repository storage", url);
                            int index = url.lastIndexOf("/");
                            if (index > 0) {
                                url = url.substring(index);
                            }
                            File destination = new File(new File(this.getLocation()), url);
                            FileOutputStream outputStream = new FileOutputStream(destination);
                            entity.writeTo(outputStream);
                            outputStream.flush();
                            outputStream.close();
                            if (update) {
                                resource = (ResourceImpl) new DataModelHelperImpl().createResource(destination.toURI().toURL());
                                LOGGER.debug("Update OBR metadata with {}", resource.getId());
                                this.addResource(resource);
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                // try to find link to "browse"
                Document document = Jsoup.connect(url).get();

                Elements links = document.select("a");
                if (links.size() > 1) {
                    for (int i = 1; i < links.size(); i++) {
                        Element link = links.get(i);
                        String absoluteHref = link.attr("abs:href");
                        this.populateFromHttp(absoluteHref, filter, update);
                    }
                }
            }
        }
    }

    /**
     * Convert the Resource absolute URI to an URI relative to the repository one.
     *
     * @param resource the Resource to manipulate.
     * @throws Exception in cave of URI conversion failure.
     */
    private void useResourceRelativeUri(ResourceImpl resource) throws Exception {
        String resourceURI = resource.getURI();
        String locationURI = "file:" + this.getLocation();
        LOGGER.debug("Converting resource URI {} relatively to repository URI {}", resourceURI, locationURI);
        if (resourceURI.startsWith(locationURI)) {
            resourceURI = resourceURI.substring(locationURI.length() + 1);
            LOGGER.debug("Resource URI converted to " + resourceURI);
            resource.put(Resource.URI, resourceURI);
        }

    }

    /**
     * Get the File object of the OBR repository.xml file.
     *
     * @return the File corresponding to the OBR repository.xml.
     * @throws Exception
     */
    private File getRepositoryXmlFile() throws Exception {
        return new File(new File(this.getLocation()), "repository.xml");
    }

    public void getResourceByUri(String uri) {
        // construct the file starting from the repository URI

    }

    /**
     * Return the OBR repository.xml corresponding to this Cave repository.
     *
     * @return the URL of the OBR repository.xml.
     * @throws Exception in case of lookup failure.
     */
    public URL getRepositoryXml() throws Exception {
        File repositoryXml = this.getRepositoryXmlFile();
        return repositoryXml.toURI().toURL();
    }

    /**
     * Delete the repository storage folder.
     *
     * @throws Exception in case of destroy failure.
     */
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(new File(this.getLocation()));
    }

}
