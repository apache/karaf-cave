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

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of the CaveRepositoryService.
 */
public class CaveRepositoryServiceImpl implements CaveRepositoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryServiceImpl.class);

    public static final String STORAGE_FILE = "repositories.properties";

    private File storageLocation;

    private Map<String, CaveRepository> repositories = new HashMap<String, CaveRepository>();

    public File getStorageLocation() {
        return this.storageLocation;
    }

    public void setStorageLocation(File storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository
     * @param scan if true, the repository is scanned at creation time.
     * @return  the Cave repository.
     * @throws Exception in case of creation failure.
     */
    public synchronized CaveRepository create(String name, boolean scan) throws Exception {
        File location = new File(storageLocation, name);
        return this.create(name, location.getAbsolutePath(), scan);
    }

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository.
     * @param location the storage location of the repository.
     * @param scan if true, the repository is scanned at creation time.
     * @return the Cave repository.
     * @throws Exception in case of creation failure.
     */
    public synchronized CaveRepository create(String name, String location, boolean scan) throws Exception {
        if (repositories.get(name) != null) {
            throw new IllegalArgumentException("Cave repository " + name + " already exists");
        }
        CaveRepository repository = new CaveRepositoryImpl(name, location, scan);
        repositories.put(name, repository);
        save();
        return repository;
    }

    /**
     * Uninstall a Cave repository from the repositories registry.
     *
     * @param name the name of Cave repository to remove.
     * @throws Exception in case of remove failure.
     */
    public synchronized void uninstall(String name) throws Exception {
        CaveRepository repository = this.getRepository(name);
        if (repository == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        // TODO: unregister Repository service
        save();
    }

    /**
     * Remove a Cave repository from the repositories registry.
     * @param name the name of the repository.
     * @throws Exception
     */
    public synchronized void remove(String name) throws Exception {
        CaveRepository repository = this.getRepository(name);
        if (repository == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        repositories.remove(name);
    }

    /**
     * Destroy a Cave repository. It removes the repository from the repositories registry
     * and cleanup the repository storage.
     *
     * @param name the name of the repository.
     * @throws Exception
     */
    public synchronized void destroy(String name) throws Exception {
        CaveRepository repository = this.getRepository(name);
        if (repository == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        repositories.remove(name);
        repository.cleanup();
    }

    /**
     * Install a Cave repository in the OBR service.
     * NB: this method allows refresh the repository in the OBR "client".
     *
     * @param name the name of the Cave repository.
     * @throws Exception in case of registration failure.
     */
    public synchronized void install(String name) throws Exception {
        CaveRepository caveRepository = this.getRepository(name);
        if (caveRepository == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        // TODO: register the Repository service
    }

    /**
     * Get the list of all Cave repositories.
     *
     * @return the list of all Cave repositories.
     */
    public synchronized CaveRepository[] getRepositories() {
        return repositories.values().toArray(new CaveRepository[0]);
    }

    /**
     * Get the Cave repository identified by name.
     *
     * @param name the name of the Cave repository to look for.
     * @return the corresponding Cave repository.
     */
    public synchronized CaveRepository getRepository(String name) {
        return repositories.get(name);
    }

    /**
     * Store the repositories into the properties file
     */
    synchronized void save() throws Exception {
        Properties storage = new Properties();
        CaveRepository[] repositories = this.getRepositories();
        storage.setProperty("count", Integer.toString(repositories.length));
        for (int i = 0; i < repositories.length; i++) {
            storage.setProperty("item." + i + ".name", repositories[i].getName());
            storage.setProperty("item." + i + ".location", repositories[i].getLocation());
        }
        saveStorage(storage, new File(storageLocation, STORAGE_FILE), "Cave Service storage");
    }

    /**
     * Write the Cave repositories storage properties into a file.
     *
     * @param properties the Cave repositories storage properties.
     * @param location the output file location.
     * @param comment a comment to write in the properties file.
     * @throws IOException in case of saving failure.
     */
    private void saveStorage(Properties properties, File location, String comment) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(location);
            properties.store(os, comment);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Load a storage property from a given file.
     *
     * @param location the properties file to load.
     * @return the loaded Properties.
     * @throws IOException in case of loading failure.
     */
    private Properties loadStorage(File location) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(location);
            Properties props = new Properties();
            props.load(is);
            return props;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Load the Cave repositories set from the storage properties file.
     */
    public synchronized void init() throws Exception {
        try {
            File storageFile = new File(storageLocation, STORAGE_FILE);
            Properties storage = loadStorage(storageFile);
            int count = 0;
            if (storage.getProperty("count") != null) {
                count = Integer.parseInt(storage.getProperty("count"));
            }
            for (int i = 0; i < count; i++) {
                String name = storage.getProperty("item." + i + ".name");
                String location = storage.getProperty("item." + i + ".location");
                if (name != null) {
                    CaveRepository repository = new CaveRepositoryImpl(name, location, false);
                    repositories.put(name, repository);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to load Cave repositories");
            LOGGER.trace("Unable to load Cave repositories", e);
        }
    }

}
