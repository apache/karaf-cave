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
package org.apache.karaf.cave.server.backend.api;

/**
 * Service to manipulate Cave repository.
 */
public interface CaveRepositoryService {

    /**
     * Create a new Karaf Cave repository.
     *
     * @param name the name of the repository
     * @param scan if true, the repository is scanned at creation time.
     * @return the Karaf Cave repository.
     * @throws Exception in case of creation failure.
     */
    CaveRepository createRepository(String name, boolean scan) throws Exception;

    /**
     * Create a new Karaf Cave repository.
     *
     * @param name the name of the repository.
     * @param location the storage location of the repository.
     * @param scan if true, the repostory is scanned at creation time.
     * @return the Karaf Cave repository.
     * @throws Exception in case of creation failure.
     */
    CaveRepository createRepository(String name, String location, boolean scan) throws Exception;

    /**
     * Get the list of all Karaf Cave repositories.
     *
     * @return the Karaf Cave repositories.
     */
    CaveRepository[] getRepositories();

    /**
     * Get the Karaf Cave repository identified by the given name.
     *
     * @param name the name of the Karaf Cave repository to look for.
     * @return the Karaf Cave repository
     */
    CaveRepository getRepository(String name);

}
