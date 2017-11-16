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
     * Explode a KAR file and upload the content on a Maven repository.
     *
     * @param karUrl The location of the KAR file.
     * @param repositoryUrl The location of the Maven repository where to upload.
     * @throws Exception in case of failure.
     */
    void explodeKar(String karUrl, String repositoryUrl) throws Exception;

    /**
     * Download an artifact from a given URL and copy it on a local filesystem.
     *
     * @param artifactUrl The artifact URL.
     * @param localUrl The local filesystem URL.
     * @throws Exception in case of failure.
     */
    void downloadArtifact(String artifactUrl, String localUrl) throws Exception;

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
    void uploadArtifact(String groupId, String artifactId, String version, String artifactUrl, String repositoryUrl)
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
     * A simple remote deployment operation for bundle. You can deploy a bundle to a remote Karaf instance.
     *
     * @param artifactUrl The location of the bundle to deploy.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void deployBundle(String artifactUrl, String jmxUrl, String karafName, String user, String password)
        throws Exception;

    /**
     * A simple remote undeploy operation for bundle. You can undeploy a bundle from a remote Karaf instance.
     *
     * @param id The bundle ID to undeploy.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void undeployBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * A simple remote start operation for bundle. You can start a bundle on a remote Karaf instance.
     *
     * @param id The bundle ID to start.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void startBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * A simple remote stop operation for bundle. You can stop a bundle on a remote Karaf instance.
     *
     * @param id The bundle ID to stop.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void stopBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to list bundles on a remote Karaf instance.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user Te username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of bundles.
     * @throws Exception in case of failure.
     */
    List<Bundle> listBundles(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to install a KAR on a remote Karaf instance.
     *
     * @param karUrl The location of the KAR file.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void installKar(String karUrl, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to uninstall a KAR from a remote Karaf instance.
     *
     * @param id The name/id of the deployed KAR.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void uninstallKar(String id, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to list the KAR on a remote Karaf instance.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of KAR.
     * @throws Exception in case of failure.
     */
    List<String> listKars(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to add a features repository to a remote Karaf instance.
     *
     * @param featuresRepositoryUrl The location of the features repository to add.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void addFeaturesRepository(String featuresRepositoryUrl, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to remove a features repository from a remote Karaf instance.
     *
     * @param featuresRepositoryUrl The location of the features repository to remove.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void removeFeaturesRepository(String featuresRepositoryUrl, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation listing the features repository on a remote Karaf instance.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of features repositories.
     * @throws Exception in case of failure.
     */
    List<FeaturesRepository> listFeaturesRepositories(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to install a feature on a remote Karaf instance.
     *
     * @param feature The feature to install.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void installFeature(String feature, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to uninstall a feature from a remote Karaf instance.
     *
     * @param feature The feature to uninstall.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void uninstallFeature(String feature, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to list features available on a remote Karaf instance.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of features.
     * @throws Exception in case of failure.
     */
    List<Feature> listFeatures(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to list features installed on a remote Karaf instance.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of features.
     * @throws Exception in case of failure.
     */
    List<String> listInstalledFeatures(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to create an empty configuration on a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void createConfig(String pid, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to get the properties of a given configuration on a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The configuration properties.
     * @throws Exception in case of failure.
     */
    Map<String, String> getConfigProperties(String pid, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to update a configuration in a remote Karaf instance.
     *
     * @param config The configuration.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void updateConfig(Config config, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to delete a configuration from a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void deleteConfig(String pid, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to append a value of the end of the current one for a config property on a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param value The value to append at the end of the current property value.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void appendConfigProperty(String pid, String key, String value, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to set a value of a config property on a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param value The new configuration property value.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void setConfigProperty(String pid, String key, String value, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to get the value of a config property on a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The current config property value.
     * @throws Exception in case of failure.
     */
    String getConfigProperty(String pid, String key, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to delete a config property from a remote Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void deleteConfigProperty(String pid, String key, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation listing the Cellar cluster nodes if available.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of Cellar cluster nodes if available.
     * @throws Exception in case of failure.
     */
    List<String> clusterNodes(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation getting the Cellar cluster groups if available.
     *
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return The list of Cellar cluster groups if available.
     * @throws Exception in case of failure.
     */
    Map<String, List<String>> clusterGroups(String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to add a features repository to a Cellar cluster group if available.
     *
     * @param repositoryId The features repository URL or ID.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void clusterAddFeaturesRepository(String repositoryId, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to remove a features repository from a Cellar cluster group if available.
     *
     * @param repositoryId The features repository URL or ID.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void clusterRemoveFeaturesRepository(String repositoryId, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to check if a features repository is present on a given Cellar cluster group.
     *
     * @param repositoryId The features repository URL or ID.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return True if the features repository is present in the Cellar cluster group, false else.
     * @throws Exception in case of failure.
     */
    boolean isFeaturesRepositoryOnClusterGroup(String repositoryId, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to check if a features repository is present on a remote Karaf instance.
     *
     * @param repositoryId The features repository URL or ID.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return True if the features repository is present on the Karaf instance, false else.
     * @throws Exception in case of failure.
     */
    boolean isFeaturesRepositoryLocal(String repositoryId, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to install a feature on a Cellar cluster group.
     *
     * @param feature The feature to install on the Cellar cluster group.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void clusterFeatureInstall(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to uninstall a feature on a Cellar cluster group.
     *
     * @param feature The feature to uninstall from the Cellar cluster group.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @throws Exception in case of failure.
     */
    void clusterFeatureUninstall(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to check if a feature is present on a given Cellar cluster group.
     *
     * @param feature The feature.
     * @param clusterGroup The Cellar cluster group.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return True if the feature is present in the Cellar cluster group, false else.
     * @throws Exception in case of failure.
     */
    boolean isFeatureOnClusterGroup(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception;

    /**
     * Simple remote operation to check if a feature is present on a given Karaf instance.
     *
     * @param feature The feature.
     * @param jmxUrl The JMX location of the target Karaf instance.
     * @param karafName The name of the Karaf instance.
     * @param user The username for the MBean server.
     * @param password The password for the MBean server.
     * @return True if the feature is present, false else.
     * @throws Exception in case of failure.
     */
    boolean isFeatureLocal(String feature, String jmxUrl, String karafName, String user, String password) throws Exception;

}
