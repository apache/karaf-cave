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
package org.apache.karaf.cave.repository;

import java.util.Collection;

/**
 * Manage Cave repositories.
 */
public interface RepositoryService {

    /**
     * Create a repository with a given name.
     *
     * @param name the repository name.
     * @return the {@link Repository} created.
     */
    Repository create(String name) throws Exception;

    /**
     * Create a repository with a given name and a storage location.
     *
     * @param name the repository name.
     * @param location the repository storage location.
     * @return the {@link Repository} created.
     */
    Repository create(String name, String location) throws Exception;

    /**
     * Create a repositoty with a given name, storage location, and proxy URL.
     *
     * @param name the repository name.
     * @param location the repository storage location.
     * @param proxy the repository proxy URL.
     * @return the {@link Repository} created.
     */
    Repository create(String name, String location, String proxy) throws Exception;

    /**
     * Create a repository with a given name, storage location, proxy and mirror repositories.
     *
     * @param name the repository name.
     * @param location the repository storage location.
     * @param proxy the repository proxy URL.
     * @param mirror the repository proxy mode (true for mirroring, false else).
     * @return the {@link Repository} created.
     */
    Repository create(String name, String location, String proxy, boolean mirror) throws Exception;

    /**
     * Create a repository with a given name, a storage location and all security settings (realm and user roles).
     *
     * @param name the repository name.
     * @param location the repository storage location.
     * @param url the repository HTTP URL.
     * @param proxy the repositories proxied by this repository.
     * @param mirror the repository proxy mode (true for mirroring, false else).
     * @param realm the JAAS realm name.
     * @param downloadRole the user role name allowed to download artifacts on this repository.
     * @param uploadRole the user role name allowed to upload artifacts on this repository.
     * @param scheduling the scheduling on this repository.
     * @param schedulingAction the action performed at scheduling time.
     * @param poolSize the pool size used internally by the repository Maven servlet.
     * @return the {@link Repository} created.
     */
    Repository create(String name, String location, String url, String proxy, boolean mirror, String realm, String downloadRole, String uploadRole, String scheduling, String schedulingAction, int poolSize) throws Exception;

    /**
     * Remove a repository.
     * NB: by default, the repository storage is not cleanup.
     *
     * @param name the repository name.
     */
    void remove(String name) throws Exception;

    /**
     * Remove a repository, eventually cleaning the repository storage.
     *
     * @param name the repository name.
     * @param storageCleanup true to cleanup the repository storage, false else.
     */
    void remove(String name, boolean storageCleanup) throws Exception;

    /**
     * Purge the storage of an existing repository.
     *
     * @param name the repository name.
     */
    void purge(String name) throws Exception;

    /**
     * Change the location of an existing repository.
     *
     * @param name the repository name.
     * @param location the new repository location.
     */
    void changeLocation(String name, String location) throws Exception;

    /**
     * Change the URL of an existing repository.
     *
     * @param name the repository name.
     * @param url the new repository URL.
     */
    void changeUrl(String name, String url) throws Exception;

    /**
     * Change the proxy URL of an existing repository.
     *
     * @param name the repository name.
     * @param proxy the new repository proxy URL.
     * @param mirror the new repository proxy mode (true for mirroring, false else).
     */
    void changeProxy(String name, String proxy, boolean mirror) throws Exception;

    /**
     * Change the repository security configuration.
     *
     * @param name the repository name.
     * @param realm the JAAS realm to use with.
     * @param downloadRole the JAAS role used for download.
     * @param uploadRole the JAAS role used for upload.
     */
    void changeSecurity(String name, String realm, String downloadRole, String uploadRole) throws Exception;

    /**
     * Change the repository scheduling configuration.
     *
     * @param name the repository name.
     * @param scheduling the scheduling (cron or trigger) for this repository.
     * @param schedulingAction the action performed at scheduling time.
     */
    void changeScheduling(String name, String scheduling, String schedulingAction) throws Exception;

    /**
     * Copy storage of a repository into another repository.
     *
     * @param sourceRepository the source repository name.
     * @param destinationRepository the destination repository name.
     */
    void copy(String sourceRepository, String destinationRepository) throws Exception;

    /**
     * Get the list of existing repositories.
     *
     * @return the {@link Collection} of repositories.
     */
    Collection<Repository> repositories();

    /**
     * Get a repository identified by a name.
     *
     * @param name the repository name.
     * @return the corresponding {@link Repository} or {@code null} if it doesn't exist.
     */
    Repository repository(String name);

    /**
     * Add an artifact in the repository identified by the given name.
     *
     * @param url the artifact URL.
     * @param name the repository name.
     */
    void addArtifact(String url, String name) throws Exception;

    /**
     * Add an artifact in the repository identified by the given name, using provided Maven coordinates.
     *
     * @param url the artifact URL.
     * @param groupId the artifact groupId.
     * @param artifactId the artifact artifactId.
     * @param version the artifact version.
     * @param type the artifact type.
     * @param classifier the artifact classifier (or null).
     * @param name the repository name.
     */
    void addArtifact(String url, String groupId, String artifactId, String version, String type, String classifier, String name) throws Exception;

    /**
     * Delete an artifact in the given repository.
     *
     * @param artifactUrl the artifact URL in the repository (could be relative path or mvn URL).
     * @param name the repository name.
     */
    void deleteArtifact(String artifactUrl, String name) throws Exception;

    /**
     * Delete an artifact (identified by Maven coordinates) in the given repository.
     *
     * @param groupId the artifact groupId.
     * @param artifactId the artifact artifactId.
     * @param version the artifact version.
     * @param type the artifact type.
     * @param classifier the artifact classifier.
     * @param name the repository name.
     */
    void deleteArtifact(String groupId, String artifactId, String version, String type, String classifier, String name) throws Exception;

    /**
     * Create/update bundle repository.xml (formerly OBR) for the given repository.
     *
     * @param name the repository name.
     */
    void updateBundleRepositoryDescriptor(String name) throws Exception;

}
