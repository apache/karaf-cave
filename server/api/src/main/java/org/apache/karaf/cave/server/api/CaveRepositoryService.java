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
package org.apache.karaf.cave.server.api;

/**
 * Service to manipulate a Cave repository.
 */
public interface CaveRepositoryService {

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository
     * @param scan if true, the repository is scanned at creation time, and the OBR metadata are created.
     * @return the Cave repository.
     * @throws Exception in case of creation failure.
     */
    CaveRepository create(String name, boolean scan) throws Exception;

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository.
     * @param location the storage location of the repository.
     * @param scan if true, the repository is scanned at creation time, and the OBR metadata are created.
     * @return the Cave repository.
     * @throws Exception in case of creation failure.
     */
    CaveRepository create(String name, String location, boolean scan) throws Exception;

    /**
     * Uninstall a Cave repository from the OBR service.
     *
     * @param name the name of the repository.
     * @throws Exception in case of uninstall failure.
     */
    void uninstall(String name) throws Exception;

    /**
     * Remove a Cave repository from the repositories registry.
     *
     * @param name the name of the repository.
     * @throws Exception in case of remove failure.
     */
    void remove(String name) throws Exception;

    /**
     * Destroy a Cave repository, including the storage.
     *
     * @param name the name of the repository.
     * @throws Exception incase of remove failure.
     */
    void destroy(String name) throws Exception;

    /**
     * Install a Cave repository into the OBR service.
     *
     * @param name the name of the Cave repository.
     * @throws Exception in case of registration failure.
     */
    void install(String name) throws Exception;

    /**
     * Get the list of all Cave repositories.
     *
     * @return the Cave repositories.
     */
    CaveRepository[] getRepositories();

    /**
     * Get a Cave repository identified by the given name.
     *
     * @param name the name of the Cave repository.
     * @return the Cave repository
     */
    CaveRepository getRepository(String name);

}
