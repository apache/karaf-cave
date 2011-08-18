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
package org.apache.karaf.cave.server.backend.impl;

import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.karaf.cave.server.backend.api.CaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;

/**
 * Default implementation of a Karaf Cave repository.
 */
public class CaveRepositoryImpl implements CaveRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryImpl.class);

    private String name;
    private File location;

    private RepositoryImpl obrRepository;

    public CaveRepositoryImpl(String name, File location, boolean scan) throws Exception {
        this.name = name;
        this.location = location;

        this.createRepositoryDirectory();
        if (scan) {
            this.scan();
        }
    }

    /**
     * Check if the repository folder exists and create it if not.
     */
    private void createRepositoryDirectory() throws Exception {
        LOGGER.debug("Create Karaf Cave repository {} folder.", name);
        if (!location.exists()) {
            location.mkdirs();
            LOGGER.debug("Karaf Cave repository {} location has been created.", name);
            LOGGER.debug(location.getAbsolutePath());
        }
        File repositoryXml = new File(location, "repository.xml");
        if (repositoryXml.exists()) {
            obrRepository = (RepositoryImpl) new DataModelHelperImpl().repository(repositoryXml.toURI().toURL());
        } else {
            obrRepository = new RepositoryImpl();
            obrRepository.setName(name);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getLocation() {
        return this.location;
    }

    public void setLocation(File location) {
        this.location = location;
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
        File temp = new File(location, artifactName);
        FileOutputStream fos = new FileOutputStream(temp);
        InputStream stream = url.openStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        stream.close();
        fos.flush();
        fos.close();
        // update the repository.xml
        ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(temp.toURI().toURL());
        if (resource == null) {
            temp.delete();
            LOGGER.warn("The {} artifact source is not a valid OSGi bundle", url);
            return;
        }
        File destination = new File(location, resource.getSymbolicName() + "-" + resource.getVersion() + ".jar");
        temp.renameTo(destination);
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
        this.scan(location);
        this.generateRepositoryXml();
    }

    /**
     * Proxy an URL (by adding repository.xml OBR information) in the Karaf Cave repository.
     *
     * @param url the URL to proxy. the URL to proxy.
     * @throws Exception
     */
    public void proxy(URL url) throws Exception {

    }

    /**
     * Recursive method to traverse all file in the repository.
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
                ResourceImpl resource = (ResourceImpl) new DataModelHelperImpl().createResource(entry.toURI().toURL());
                this.addResource(resource);
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Convert the Resource absolute URI to an URI relative to the repository one.
     * @param resource the Resource to manipulate.
     * @throws Exception in cave of URI convertion failure.
     */
    private void useResourceRelativeUri(ResourceImpl resource) throws Exception {
        String resourceURI = resource.getURI();
        String repositoryURI = location.toURI().toString();
        LOGGER.debug("Converting resource URI " + resourceURI + " relatively to repository URI " + repositoryURI);
        if (resourceURI.startsWith(repositoryURI)) {
            resourceURI = resourceURI.substring(repositoryURI.length());
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
        return new File(location, "repository.xml");
    }

    /**
     * Return the OBR repository.xml corresponding to this Karaf Cave repository.
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
        location.delete();
    }

}
