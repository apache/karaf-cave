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
package org.apache.karaf.cave.deployer.api;

import java.util.List;
import java.util.Map;

public interface Deployer {

    /**
     * Register a connection in the Deployer service.
     */
    void registerConnection(Connection connection) throws Exception;

    /**
     * Delete a connection from the Deployer service.
     */
    void deleteConnection(String connectionName) throws Exception;

    /**
     * Get the connections registered in the Deployer service.
     */
    List<Connection> connections() throws Exception;

    /**
     * Explode a file (KAR or zip) and upload the content on a Maven repository.
     *
     * @param url The location of the file.
     * @param repository The location of the Maven repository where to upload.
     * @throws Exception in case of failure.
     */
    void explode(String url, String repository) throws Exception;

    /**
     * Extract a file (KAR or zip) to a local Karaf directory.
     *
     * @param url The location of the file.
     * @param directory The location of the directory where to extract.
     * @throws Exception in case of failure.
     */
    void extract(String url, String directory) throws Exception;

    /**
     * Download an artifact from a given URL and copy it on a local filesystem.
     *
     * @param artifact The artifact URL.
     * @param directory The local directory.
     * @throws Exception in case of failure.
     */
    void download(String artifact, String directory) throws Exception;

    /**
     * Upload an artifact to a Maven repository using the provided Maven coordinates.
     *
     * @param groupId The resulting artifact groupId.
     * @param artifactId The resulting artifact artifactId.
     * @param version The resulting artifact version.
     * @param artifactUrl The source artifact location.
     * @param repositoryUrl The location of the Maven repository where to upload.
     * @throws Exception in case of failure.
     */
    void upload(String groupId, String artifactId, String version, String artifactUrl, String repositoryUrl)
        throws Exception;

    /**
     * Create/assemble a feature based on existing ones. It allows you to create "meta" feature.
     *
     * @param groupId The resulting features repository groupId.
     * @param artifactId The resulting features repository artifactId.
     * @param version The resulting features repository version.
     * @param repositoryUrl The location of the Maven repository where to upload the features repository.
     * @param feature The name of the resulting feature.
     * @param featureRepositoryUrls The source features repository URLs.
     * @param features The source feature names.
     * @param bundles The source bundle locations.
     * @param configs The source configuration PIDs.
     * @throws Exception in case of failure.
     */
    void assembleFeature(String groupId, String artifactId, String version, String repositoryUrl, String feature,
                         List<String> featureRepositoryUrls,
                         List<String> features,
                         List<String> bundles,
                         List<Config> configs) throws Exception;

    /**
     * A simple remote deployment operation for bundle. You can install a bundle to a remote Karaf instance.
     */
    void installBundle(String artifactUrl, String connection)
        throws Exception;

    /**
     * A simple remote undeploy operation for bundle. You can undeploy a bundle from a remote Karaf instance.
     */
    void uninstallBundle(String id, String connection) throws Exception;

    /**
     * A simple remote start operation for bundle. You can start a bundle on a remote Karaf instance.
     */
    void startBundle(String id, String connection) throws Exception;

    /**
     * A simple remote stop operation for bundle. You can stop a bundle on a remote Karaf instance.
     */
    void stopBundle(String id, String connection) throws Exception;

    /**
     * Simple remote operation to list bundles on a remote Karaf instance.
     */
    List<Bundle> bundles(String connection) throws Exception;

    /**
     * Simple remote operation to install a KAR on a remote Karaf instance.
     */
    void installKar(String karUrl, String connection) throws Exception;

    /**
     * Simple remote operation to uninstall a KAR from a remote Karaf instance.
     */
    void uninstallKar(String id, String connection) throws Exception;

    /**
     * Simple remote operation to list the KAR on a remote Karaf instance.
     */
    List<String> kars(String connection) throws Exception;

    /**
     * Simple remote operation to add a features repository to a remote Karaf instance.
     */
    void addFeaturesRepository(String featuresRepositoryUrl, String connection) throws Exception;

    /**
     * Simple remote operation to remove a features repository from a remote Karaf instance.
     */
    void removeFeaturesRepository(String featuresRepositoryUrl, String connection) throws Exception;

    /**
     * Simple remote operation listing the features repository on a remote Karaf instance.
     */
    List<FeaturesRepository> featuresRepositories(String connection) throws Exception;

    /**
     * Simple remote operation to install a feature on a remote Karaf instance.
     */
    void installFeature(String feature, String connection) throws Exception;

    /**
     * Simple remote operation to uninstall a feature from a remote Karaf instance.
     */
    void uninstallFeature(String feature, String connection) throws Exception;

    /**
     * Simple remote operation to list features available on a remote Karaf instance.
     */
    List<Feature> features(String connection) throws Exception;

    /**
     * Simple remote operation to list features installed on a remote Karaf instance.
     */
    List<String> installedFeatures(String connection) throws Exception;

    /**
     * Simple remote operation to create an empty configuration on a remote Karaf instance.
     */
    void createConfig(String pid, String connection) throws Exception;

    /**
     * Simple remote operation to get the properties of a given configuration on a remote Karaf instance.
     */
    Map<String, String> configProperties(String pid, String connection) throws Exception;

    /**
     * Simple remote operation to update a configuration in a remote Karaf instance.
     */
    void updateConfig(Config config, String connection) throws Exception;

    /**
     * Simple remote operation to delete a configuration from a remote Karaf instance.
     */
    void deleteConfig(String pid, String connection) throws Exception;

    /**
     * Simple remote operation to append a value of the end of the current one for a config property on a remote Karaf instance.
     */
    void appendConfigProperty(String pid, String key, String value, String connection) throws Exception;

    /**
     * Simple remote operation to set a value of a config property on a remote Karaf instance.
     */
    void setConfigProperty(String pid, String key, String value, String connection) throws Exception;

    /**
     * Simple remote operation to get the value of a config property on a remote Karaf instance.
     */
    String configProperty(String pid, String key, String connection) throws Exception;

    /**
     * Simple remote operation to delete a config property from a remote Karaf instance.
     */
    void deleteConfigProperty(String pid, String key, String connection) throws Exception;

    /**
     * Simple remote operation listing the Cellar cluster nodes if available.
     */
    List<String> clusterNodes(String connection) throws Exception;

    /**
     * Simple remote operation getting the Cellar cluster groups if available.
     */
    Map<String, List<String>> clusterGroups(String connection) throws Exception;

    /**
     * Simple remote operation to add a features repository to a Cellar cluster group if available.
     */
    void clusterAddFeaturesRepository(String url, String clusterGroup, String connection) throws Exception;

    /**
     * Simple remote operation to remove a features repository from a Cellar cluster group if available.
     */
    void clusterRemoveFeaturesRepository(String url, String clusterGroup, String Connection) throws Exception;

    /**
     * Simple remote operation to check if a features repository is present on a given Cellar cluster group.
     */
    boolean isFeaturesRepositoryOnClusterGroup(String repositoryId, String clusterGroup, String connection) throws Exception;

    /**
     * Simple remote operation to check if a features repository is present on a remote Karaf instance.
     */
    boolean isFeaturesRepositoryLocal(String repositoryId, String connection) throws Exception;

    /**
     * Simple remote operation to install a feature on a Cellar cluster group.
     */
    void clusterFeatureInstall(String feature, String clusterGroup, String connection) throws Exception;

    /**
     * Simple remote operation to uninstall a feature on a Cellar cluster group.
     */
    void clusterFeatureUninstall(String feature, String clusterGroup, String connection) throws Exception;

    /**
     * Simple remote operation to check if a feature is present on a given Cellar cluster group.
     */
    boolean isFeatureOnClusterGroup(String feature, String clusterGroup, String connection) throws Exception;

    /**
     * Simple remote operation to check if a feature is present on a given Karaf instance.
     */
    boolean isFeatureLocal(String feature, String connection) throws Exception;

}
