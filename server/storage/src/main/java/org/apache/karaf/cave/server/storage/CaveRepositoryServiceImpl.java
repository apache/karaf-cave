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

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the Cave Repository Service.
 */
public class CaveRepositoryServiceImpl implements CaveRepositoryService {

    private File storageLocation;
    private RepositoryAdmin repositoryAdmin;

    private Map<String, CaveRepository> repositories = new HashMap<String, CaveRepository>();

    public File getStorageLocation() {
        return this.storageLocation;
    }

    public void setStorageLocation(File storageLocation) {
        this.storageLocation = storageLocation;
    }

    public RepositoryAdmin getRepositoryAdmin() {
        return this.repositoryAdmin;
    }

    public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }

    /**
     * Create a new Karaf Cave repository.
     *
     * @param name the name of the repository
     * @param scan if true, the repository is scanned at creation time.
     * @return  the Karaf Cave repository.
     * @throws Exception in case of creation failure.
     */
    public synchronized CaveRepository createRepository(String name, boolean scan) throws Exception {
        File location = new File(storageLocation, name);
        return this.createRepository(name, location.getAbsolutePath(), scan);
    }

    /**
     * Create a new Karaf Cave repository.
     *
     * @param name the name of the repository.
     * @param location the storage location of the repository.
     * @param scan if true, the repostory is scanned at creation time.
     * @return the Karaf Cave repository.
     * @throws Exception in case of creation failure.
     */
    public synchronized CaveRepository createRepository(String name, String location, boolean scan) throws Exception {
        if (repositories.get(name) != null) {
            throw new IllegalArgumentException("Cave repository " + name + " already exists.");
        }
        CaveRepository repository = new CaveRepositoryImpl(name, location, scan);
        repositories.put(name, repository);
        return repository;
    }

    /**
     * Remove a Karaf Cave repository from the repositories registry.
     *
     * @param name the name of Karaf Cave repository to remove.
     * @throws Exception in case of remove failure.
     */
    public synchronized void remove(String name) throws Exception {
        CaveRepository repository = this.getRepository(name);
        if (repository != null) {
            repositoryAdmin.removeRepository(repository.getRepositoryXml().toString());
            repositories.remove(name);
        } else {
            throw new IllegalArgumentException("Cave repository " + name + " not found.");
        }
    }

    /**
     * Register a Karaf Cave repository in the OBR service.
     * NB: this method allows refresh the repository in the OBR "client".
     *
     * @param name the name of the Karaf Cave repository.
     * @throws Exception in case of registration failure.
     */
    public synchronized void register(String name) throws Exception {
        CaveRepository caveRepository = this.getRepository(name);
        if (caveRepository != null) {
            repositoryAdmin.addRepository(caveRepository.getRepositoryXml());
        } else {
            throw new IllegalArgumentException("Cave repository " + name + " not found.");
        }
    }

    /**
     * Get the list of all Karaf Cave repositories.
     *
     * @return the list of all Karaf Cave repositories.
     */
    public synchronized CaveRepository[] getRepositories() {
        return repositories.values().toArray(new CaveRepository[0]);
    }

    /**
     * Get the Karaf Cave repository identified by name.
     *
     * @param name the name of the Karaf Cave repository to look for.
     * @return the corresponding Karaf Cave repository.
     */
    public synchronized CaveRepository getRepository(String name) {
        return repositories.get(name);
    }

}
