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
package org.apache.karaf.cave.deployer.management.internal;

import org.apache.karaf.cave.deployer.api.*;
import org.apache.karaf.cave.deployer.management.CaveDeployerMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.*;
import java.util.List;
import java.util.Map;

public class CaveDeployerMBeanImpl extends StandardMBean implements CaveDeployerMBean {

    private Deployer deployer;

    public CaveDeployerMBeanImpl() throws NotCompliantMBeanException {
        super(CaveDeployerMBean.class);
    }

    public Deployer getDeployer() {
        return deployer;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }


    @Override
    public void registerConnection(String name, String jmxUrl, String karafName, String user, String password) throws Exception {
        Connection connection = new Connection();
        connection.setName(name);
        connection.setJmxUrl(jmxUrl);
        connection.setKarafName(karafName);
        connection.setUser(user);
        connection.setPassword(password);
        deployer.registerConnection(connection);
    }

    @Override
    public void deleteConnection(String name) throws Exception {
        deployer.deleteConnection(name);
    }

    @Override
    public TabularData getConnections() throws Exception {
        List<Connection> connections = deployer.connections();

        CompositeType connectionType = new CompositeType("Connection", "Connection to a Karaf instance",
                new String[]{"Name", "JMX URL", "Karaf Name", "User", "Password"},
                new String[]{"Name of the connection", "JMX URL of the Karaf instance", "Karaf instance name", "Username to connect", "Password of the user" },
                new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});
        TabularType tabularType = new TabularType("Connections", "Table of all Cave Deployer connections", connectionType, new String[]{"Name"});
        TabularData table = new TabularDataSupport(tabularType);
        for (Connection connection : connections) {
            CompositeData data = new CompositeDataSupport(connectionType,
                    new String[]{"Name", "JMX URL", "Karaf Name", "User", "Password"},
                    new Object[]{connection.getName(), connection.getJmxUrl(), connection.getKarafName(), connection.getUser(), connection.getPassword()});
            table.put(data);
        }
        return table;
    }

    @Override
    public void explode(String url, String repository) throws Exception {
        deployer.explode(url, repository);
    }

    @Override
    public void extract(String url, String directory) throws Exception {
        deployer.extract(url, directory);
    }

    @Override
    public void download(String url, String directory) throws Exception {
        deployer.download(url, directory);
    }

    @Override
    public void upload(String groupId, String artifactId, String version, String artifactUrl, String repositoryUrl) throws Exception {
        deployer.upload(groupId, artifactId, version, artifactUrl, repositoryUrl);
    }

    @Override
    public void assembleFeature(String groupId, String artifactId, String version, String repositoryUrl, String feature, List<String> repositories, List<String> features, List<String> bundles) throws Exception {
        deployer.assembleFeature(groupId, artifactId, version, repositoryUrl, feature, repositories, features, bundles, null);
    }

    @Override
    public void installBundle(String url, String connection) throws Exception {
        deployer.installBundle(url, connection);
    }

    @Override
    public void uninstallBundle(String id, String connection) throws Exception {
        deployer.uninstallBundle(id, connection);
    }

    @Override
    public void startBundle(String id, String connection) throws Exception {
        deployer.startBundle(id, connection);
    }

    @Override
    public void stopBundle(String id, String connection) throws Exception {
        deployer.stopBundle(id, connection);
    }

    @Override
    public TabularData getBundles(String connection) throws Exception {
        CompositeType bundleType = new CompositeType("Bundle", "A bundle on the remote instance",
                new String[]{"ID", "Name", "Version", "Start Level", "State"},
                new String[]{"Bundle ID", "Bundle Name", "Bundle Version", "Bundle Start Level", "Bundle State"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER, SimpleType.STRING});
        TabularType tableType = new TabularType("Bundles", "Table of bundles", bundleType, new String[]{"ID"});
        TabularData table = new TabularDataSupport(tableType);

        List<Bundle> bundles = deployer.bundles(connection);

        for (Bundle bundle : bundles) {
            CompositeData data = new CompositeDataSupport(bundleType,
                    new String[]{"ID", "Name", "Version", "Start Level", "State"},
                    new Object[]{bundle.getId(), bundle.getName(), bundle.getVersion(), bundle.getStartLevel(), bundle.getState()});
            table.put(data);
        }

        return table;
    }

    @Override
    public void installKar(String url, String connection) throws Exception {
        deployer.installKar(url, connection);
    }

    @Override
    public void uninstallKar(String id, String connection) throws Exception {
        deployer.uninstallKar(id, connection);
    }

    @Override
    public List<String> getKars(String connection) throws Exception {
        return deployer.kars(connection);
    }

    @Override
    public void addFeatureRepository(String url, String connection) throws Exception {
        deployer.addFeaturesRepository(url, connection);
    }

    @Override
    public void removeFeatureRepository(String repository, String connection) throws Exception {
        deployer.removeFeaturesRepository(repository, connection);
    }

    @Override
    public TabularData getFeatureRepositories(String connection) throws Exception {
        List<FeaturesRepository> repositories = deployer.featuresRepositories(connection);

        CompositeType repositoryType = new CompositeType("Features Repository", "Features Repository",
                new String[]{"Name", "URL"},
                new String[]{"The features repository name", "The location of the features repository"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING});
        TabularType tableType = new TabularType("Features Repositories", "Table of features repositories", repositoryType, new String[]{"Name"});
        TabularData table = new TabularDataSupport(tableType);

        for (FeaturesRepository repository : repositories) {
            CompositeData data = new CompositeDataSupport(repositoryType,
                    new String[]{"Name", "URL"},
                    new Object[]{repository.getName(), repository.getUri()});
            table.put(data);
        }
        return table;
    }

    @Override
    public void installFeature(String feature, String connection) throws Exception {
        deployer.installFeature(feature, connection);
    }

    @Override
    public void uninstallFeature(String feature, String connection) throws Exception {
        deployer.uninstallFeature(feature, connection);
    }

    @Override
    public TabularData getFeatures(String connection) throws Exception {
        List<Feature> features = deployer.features(connection);

        CompositeType featureType = new CompositeType("Feature", "Feature",
                new String[]{"Name", "Version", "State"},
                new String[]{"Name of the feature", "Version of the feature", "State of the feature"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});
        TabularType tableType = new TabularType("Features", "Table of features",
                featureType, new String[]{"Name", "Version"});
        TabularData table = new TabularDataSupport(tableType);
        for (Feature feature : features) {
            CompositeData data = new CompositeDataSupport(featureType,
                    new String[]{"Name", "Version", "State"},
                    new Object[]{feature.getName(), feature.getVersion(), feature.getState()});
            table.put(data);
        }

        return table;
    }

    @Override
    public void createConfig(String pid, String connection) throws Exception {
        deployer.createConfig(pid, connection);
    }

    @Override
    public Map<String, String> getConfigProperties(String pid, String connection) throws Exception {
        return deployer.configProperties(pid, connection);
    }

    @Override
    public void deleteConfig(String pid, String connection) throws Exception {
        deployer.deleteConfig(pid, connection);
    }

    @Override
    public void appendConfigProperty(String pid, String key, String value, String connection) throws Exception {
        deployer.appendConfigProperty(pid, key, value, connection);
    }

    @Override
    public void setConfigProperty(String pid, String key, String value, String connection) throws Exception {
        deployer.setConfigProperty(pid, key, value, connection);
    }

    @Override
    public String getConfigProperty(String pid, String key, String connection) throws Exception {
        return deployer.configProperty(pid, key, connection);
    }

    @Override
    public void deleteConfigProperty(String pid, String key, String connection) throws Exception {
        deployer.deleteConfigProperty(pid, key, connection);
    }

    @Override
    public List<String> getClusterNodes(String connection) throws Exception {
        return deployer.clusterNodes(connection);
    }

    @Override
    public Map<String, List<String>> getClusterGroups(String connection) throws Exception {
        return deployer.clusterGroups(connection);
    }

    @Override
    public void clusterFeatureRepositoryAdd(String url, String clusterGroup, String connection) throws Exception {
        deployer.clusterAddFeaturesRepository(url, clusterGroup, connection);
    }

    @Override
    public void clusterFeatureRepositoryRemove(String url, String clusterGroup, String connection) throws Exception {
        deployer.clusterRemoveFeaturesRepository(url, clusterGroup, connection);
    }

    @Override
    public void clusterFeatureInstall(String feature, String clusterGroup, String connection) throws Exception {
        deployer.clusterFeatureInstall(feature, clusterGroup, connection);
    }

    @Override
    public void clusterFeatureUninstall(String feature, String clusterGroup, String connection) throws Exception {
        deployer.clusterFeatureUninstall(feature, clusterGroup, connection);
    }
}
