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

import java.net.URL;

/**
 * Cave repository is a storage area where to upload artifacts.
 * It's already the basement of the repository.xml metadata.
 */
public interface CaveRepository {

    /**
     * Get the name of the repository.
     *
     * @return the name of the repository
     */
    public String getName();

    /**
     * Get the location (filesystem) of this repository.
     *
     * @return the location of this repository.
     */
    public String getLocation();

    /**
     * Get the last modification date of this repository.
     *
     * @return the last modification date.
     */
    public long getIncrement();

    /**
     * Upload an artifact from the given URL into the repository.
     *
     * @param url the URL of the artifact.
     * @throws Exception in case of upload failure.
     */
    public void upload(URL url) throws Exception;

    /**
     * Scan the whole repository, reading bundle MANIFEST, etc to update
     * or generate the repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    public void scan() throws Exception;

    /**
     * Proxy an URL (for instance a Maven repository) and add repository metadata..
     *
     * @param url the URL to proxy.
     * @throws Exception
     */
    public void proxy(URL url) throws Exception;

    /**
     * Proxy an URL (for instance a Maven repository), eventually filtering some artifacts, and add repository metadata..
     *
     * @param url the URL to proxy.
     * @param filter regex filter on the artifacts URL.
     * @throws Exception
     */
    public void proxy(URL url, String filter) throws Exception;

    /**
     * Proxy an URL (for instance a Maven repository), eventually filtering some artifacts,
     * provide a Properties file containing URL authorization parameters and add repository metadata..
     *
     * @param url the URL to proxy.
     * @param filter regex filter on the artifacts URL.
     * @param properties a Properties file containing URL authorization parameters.
     * @throws Exception
     */
    public void proxy(URL url, String filter, String properties) throws Exception;

    /**
     * Populate from a remote URL (for instance a Maven repository), and eventually update the repository metadata.
     *
     * @param url the URL to copy.
     * @param update if true the repository metadata is updated, false else.
     * @throws Exception in case of copy failure.
     */
    public void populate(URL url, boolean update) throws Exception;

    /**
     * Populate from a remote URL (for instance a Maven repository), eventually filtering artifacts, and eventually update the repository metadata.
     *
     * @param url the URL to copy.
     * @param filter regex filter on the artifacts URL.
     * @param update if true the repository metadata is updated, false else.
     * @throws Exception
     */
    public void populate(URL url, String filter, boolean update) throws Exception;

    /**
     * Populate from a remote URL (for instance a Maven repository), eventually filtering artifacts,
     * provide a Properties file containing URL authorization parameters and eventually update the repository metadata.
     *
     * @param url the URL to copy.
     * @param filter regex filter on the artifacts URL.
     * @param properties a Properties file containing URL authorization parameters.
     * @param update if true the repository metadata is updated, false else.
     * @throws Exception
     */
    public void populate(URL url, String filter, String properties, boolean update) throws Exception;

    /**
     * Return an URL for the resource at the given URI.
     *
     * @param uri the resource URI.
     * @return the URL for the resource.
     * @throws Exception in case of read failure.
     */
    public URL getResourceByUri(String uri) throws Exception;

    /**
     * Return the output stream of the resource identified by the given ID.
     *
     * @param id the resource ID.
     * @return the output stream of the resource.
     * @throws Exception in case of read failure.
     */
    //public OutputStream getResourceById(String id) throws Exception;

    /**
     * Return the repository URL of the repository.xml.
     *
     * @return the URL of the repository.xml.
     * @throws Exception in case of failure to get repository.xml URL.
     */
    public URL getRepositoryXml() throws Exception;

    /**
     * Cleanup the repository storage.
     *
     * @throws Exception in case of cleanup failure.
     */
    public void cleanup() throws Exception;

}
