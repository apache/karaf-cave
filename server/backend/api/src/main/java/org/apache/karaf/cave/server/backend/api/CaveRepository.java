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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Cave repository is a storage area where to upload artifacts.
 * It's already the basement of the OBR repository.xml metadata.
 */
public interface CaveRepository {

    /**
     * Get the name of the repository.
     *
     * @return the name of the repository
     */
    String getName();

    /**
     * Set the name of the repository.
     *
     * @param name the name of the repository
     */
    void setName(String name);

    /**
     * Get the location (filesystem) of this repository.
     *
     * @return the location of this repository.
     */
    String getLocation();

    /**
     * Set the location (filesystem) of this repository.
     *
     * @param location the location of this repository
     */
    void setLocation(String location);

    /**
     * Upload an artifact from the given URL into the repository.
     *
     * @param url the URL of the artifact.
     * @throws Exception in case of upload failure.
     */
    void upload(URL url) throws Exception;

    /**
     * Scan the whole repository, reading bundle MANIFEST, etc to update
     * or generate the OBR repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    void scan() throws Exception;

    /**
     * Proxy an URL (for instance a Maven repository) and add OBR information.
     *
     * @param url the URL to proxy.
     * @throws Exception
     */
    void proxy(URL url) throws Exception;

    /**
     * Populate from a remote URL (for instance a Maven repository), and eventually update the OBR information.
     *
     * @param url the URL to copy.
     * @param update if true the OBR information is updated, false else.
     * @throws Exception in case of copy failure.
     */
    void populate(URL url, boolean update) throws Exception;

    /**
     * Return the repository URL of the OBR repository.xml.
     *
     * @return the URL of the OBR repository.xml.
     * @throws Exception in case of failure to get repository.xml URL.
     */
    URL getRepositoryXml() throws Exception;

    /**
     * Cleanup the repository storage.
     *
     * @throws Exception in case of cleanup failure.
     */
    void cleanup() throws Exception;

}
