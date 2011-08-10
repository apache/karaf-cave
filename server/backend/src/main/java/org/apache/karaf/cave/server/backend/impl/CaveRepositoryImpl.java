package org.apache.karaf.cave.server.backend.impl;

import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.karaf.cave.server.backend.CaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

/**
 * Default implementation of a Karaf Cave repository.
 */
public class CaveRepositoryImpl implements CaveRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryImpl.class);

    private String name;
    private String location;
    private RepositoryImpl obrRepository;

    public CaveRepositoryImpl(String name, String location) throws Exception {
        this.name = name;
        this.location = location;

        this.createRepositoryDirectory();
    }

    /**
     * Check if the repository folder exists and create it if not.
     */
    private void createRepositoryDirectory() throws Exception {
        LOGGER.debug("Create Karaf Cave repository {} folder.", name);
        File directory = new File(this.location);
        if (!directory.exists()) {
            directory.mkdirs();
            LOGGER.debug("Karaf Cave repository {} location has been created.", name);
            LOGGER.debug(location);
        }
        File repositoryXml = new File(location, "repository.xml");
        if (repositoryXml.exists()) {
            obrRepository = (RepositoryImpl) new DataModelHelperImpl().repository(repositoryXml.toURI().toURL());
        } else {
            obrRepository = new RepositoryImpl();
        }
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    /**
     * Update the repository.xml with the artifact at the given URL.
     *
     * @param resource the bundle resource
     * @throws Exception in case of repository.xml update failure.
     */
    private void updateRepositoryXml(Resource resource) throws Exception {
        if (resource != null) {
            obrRepository.addResource(resource);
            obrRepository.setLastModified(System.currentTimeMillis());
            File repositoryXml = new File(location, "repository.xml");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(repositoryXml));
            new DataModelHelperImpl().writeRepository(obrRepository, writer);
            writer.flush();
            writer.close();
        }
    }

    /**
     * Add an artifact into this repository
     *
     * @param path the location of the raw artifact
     * @throws Exception in case of upload failure
     */
    public void upload(String path) throws Exception {
        LOGGER.debug("Upload new artifact from {}", location);
        // copy the file
        LOGGER.debug("Copy the file from {}", location);
        File source = new File(path);
        if (!source.exists()) {
            throw new IllegalArgumentException("The artifact location " + location + " doesn't exist.");
        }
        File destination = new File(location, source.getName());
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(destination);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fis.close();
        fos.flush();
        fos.close();
        // update the repository.xml
        Resource resource = new DataModelHelperImpl().createResource(destination.toURI().toURL());
        if (resource == null) {
            destination.delete();
            throw new IllegalArgumentException("The " + path + " source is not a valid OSGi bundle");
        }
        this.updateRepositoryXml(resource);
    }

    /**
     * Upload an artifact using a stream.
     *
     * @param stream the artifact stream.
     * @throws Exception in case of upload failure.
     */
    public void upload(InputStream stream) throws Exception {
        LOGGER.debug("Upload new artifact from a stream");
        String artifactName = "artifact-" + System.currentTimeMillis();
        File destination = new File(location, artifactName);
        FileOutputStream fos = new FileOutputStream(destination);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
        fos.close();
        // update the repository.xml
        Resource resource = new DataModelHelperImpl().createResource(destination.toURI().toURL());
        if (resource == null) {
            destination.delete();
            throw new IllegalArgumentException("The stream source is not a valid OSGi bundle");
        }
        destination.renameTo(new File(location, resource.getSymbolicName() + "-" + resource.getVersion()));
        this.updateRepositoryXml(resource);
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
        File destination = new File(location, artifactName);
        FileOutputStream fos = new FileOutputStream(destination);
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
        Resource resource = new DataModelHelperImpl().createResource(destination.toURI().toURL());
        if (resource == null) {
            destination.delete();
            throw new IllegalArgumentException("The " + url + " artifact source is not a valid OSGi bundle");
        }
        destination.renameTo(new File(location, resource.getSymbolicName() + "-" + resource.getVersion()));
        this.updateRepositoryXml(resource);
    }

    /**
     * Scan the content of the whole repository to update the repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    public void scan() throws Exception {
        // TODO
    }

    /**
     * Register the repository (repository.xml) into the OBR (using the OBR RepositoryAdmin service).
     *
     * @throws Exception in case of register failure.
     */
    public void register() throws Exception {
        // TODO
    }

    /**
     * Destroy this repository by deleting the storage folder.
     *
     * @throws Exception in case of destroy failure.
     */
    public void destroy() throws Exception {
        File storage = new File(location);
        storage.delete();
    }

}
