/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cave.deployer.management;

import javax.management.openmbean.TabularData;
import java.util.List;
import java.util.Map;

public interface CaveDeployerMBean {

    void registerConnection(String name, String jmxUrl, String karafName, String user, String password) throws Exception;
    void deleteConnection(String name) throws Exception;
    TabularData getConnections() throws Exception;

    void explode(String url, String repository) throws Exception;
    void extract(String url, String directory) throws Exception;
    void download(String url, String directory) throws Exception;
    void upload(String groupId, String artifactId, String version, String artifactUrl, String repositoryUrl) throws Exception;

    void assembleFeature(String groupId, String artifactId, String version, String repositoryUrl, String feature,
                         List<String> repositories, List<String> features, List<String> bundles) throws Exception;

    void installBundle(String url, String connection) throws Exception;
    void uninstallBundle(String id, String connection) throws Exception;
    void startBundle(String id, String connection) throws Exception;
    void stopBundle(String id, String connection) throws Exception;
    TabularData getBundles(String connection) throws Exception;

    void installKar(String url, String connection) throws Exception;
    void uninstallKar(String id, String connection) throws Exception;
    List<String> getKars(String connection) throws Exception;

    void addFeatureRepository(String url, String connection) throws Exception;
    void removeFeatureRepository(String repository, String connection) throws Exception;
    TabularData getFeatureRepositories(String connection) throws Exception;

    void installFeature(String feature, String connection) throws Exception;
    void uninstallFeature(String feature, String connection) throws Exception;
    TabularData getFeatures(String connection) throws Exception;

    void createConfig(String pid, String connection) throws Exception;
    Map<String, String> getConfigProperties(String pid, String connection) throws Exception;
    void deleteConfig(String pid, String connection) throws Exception;
    void appendConfigProperty(String pid, String key, String value, String connection) throws Exception;
    void setConfigProperty(String pid, String key, String value, String connection) throws Exception;
    String getConfigProperty(String pid, String key, String connection) throws Exception;
    void deleteConfigProperty(String pid, String key, String connection) throws Exception;

    List<String> getClusterNodes(String connection) throws Exception;
    Map<String, List<String>> getClusterGroups(String connection) throws Exception;
    void clusterFeatureRepositoryAdd(String url, String clusterGroup, String connection) throws Exception;
    void clusterFeatureRepositoryRemove(String url, String clusterGroup, String connection) throws Exception;
    void clusterFeatureInstall(String feature, String clusterGroup, String connection) throws Exception;
    void clusterFeatureUninstall(String feature, String clusterGroup, String connection) throws Exception;

}
