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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.OutputStream;
import java.net.URL;

/**
 * Cave repository is a storage area where to upload artifacts.
 * It's already the basement of the OBR repository.xml metadata.
 */
@XmlRootElement(name = "cave-repository")
public abstract class CaveRepository {

    private String name;
    private String location;

    /**
     * Get the name of the repository.
     *
     * @return the name of the repository
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the repository.
     *
     * @param name the name of the repository
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the location (filesystem) of this repository.
     *
     * @return the location of this repository.
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Set the location (filesystem) of this repository.
     *
     * @param location the location of this repository
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Upload an artifact from the given URL into the repository.
     *
     * @param url the URL of the artifact.
     * @throws Exception in case of upload failure.
     */
    public abstract void upload(URL url) throws Exception;

    /**
     * Scan the whole repository, reading bundle MANIFEST, etc to update
     * or generate the OBR repository.xml.
     *
     * @throws Exception in case of scan failure.
     */
    public abstract void scan() throws Exception;

    /**
     * Proxy an URL (for instance a Maven repository) and add OBR information.
     *
     * @param url the URL to proxy.
     * @throws Exception
     */
    public abstract void proxy(URL url) throws Exception;

    /**
     * Populate from a remote URL (for instance a Maven repository), and eventually update the OBR information.
     *
     * @param url the URL to copy.
     * @param update if true the OBR information is updated, false else.
     * @throws Exception in case of copy failure.
     */
    public abstract void populate(URL url, boolean update) throws Exception;

    /**
     * Return the output stream of the resource at the given URI.
     *
     * @param uri the resource URI.
     * @return the output stream of the resource.
     * @throws Exception in case of read failure.
     */
    //public abstract OutputStream getResourceByUri(String uri) throws Exception;

    /**
     * Return the output stream of the resource identified by the given ID.
     *
     * @param id the resource ID.
     * @return the output stream of the resource.
     * @throws Exception in case of read failure.
     */
    //public abstract OutputStream getResourceById(String id) throws Exception;

    /**
     * Return the repository URL of the OBR repository.xml.
     *
     * @return the URL of the OBR repository.xml.
     * @throws Exception in case of failure to get repository.xml URL.
     */
    public abstract URL getRepositoryXml() throws Exception;

    /**
     * Cleanup the repository storage.
     *
     * @throws Exception in case of cleanup failure.
     */
    public abstract void cleanup() throws Exception;

}
