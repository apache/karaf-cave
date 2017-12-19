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
package org.apache.karaf.cave.deployer.rest;

import org.apache.karaf.cave.deployer.api.*;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

@Path("/")
public class DeployerRest {

    private Deployer deployer;

    public static class KarExplodeRequest {

        private String artifactUrl;
        private String repositoryUrl;

        public String getArtifactUrl() {
            return artifactUrl;
        }

        public void setArtifactUrl(String artifactUrl) {
            this.artifactUrl = artifactUrl;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

    }

    @Path("/artifact/explode")
    @Consumes("application/json")
    @POST
    public void explode(KarExplodeRequest request) throws Exception {
        deployer.explode(request.getArtifactUrl(), request.getRepositoryUrl());
    }

    public static class ExtractRequest {

        private String artifactUrl;
        private String directory;

        public String getArtifactUrl() {
            return artifactUrl;
        }

        public void setArtifactUrl(String artifactUrl) {
            this.artifactUrl = artifactUrl;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }

    @Path("/artifact/extract")
    @Consumes("application/json")
    @POST
    public void extract(ExtractRequest request) throws Exception {
        deployer.extract(request.getArtifactUrl(), request.getDirectory());
    }

    public static class UploadRequest {

        private String groupId;
        private String artifactId;
        private String version;
        private String artifactUrl;
        private String repositoryUrl;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getArtifactUrl() {
            return artifactUrl;
        }

        public void setArtifactUrl(String artifactUrl) {
            this.artifactUrl = artifactUrl;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

    }

    @Path("/artifact/upload")
    @Consumes("application/json")
    @POST
    public void upload(UploadRequest request) throws Exception {
        deployer.upload(request.getGroupId(),
                request.getArtifactId(),
                request.getVersion(),
                request.getArtifactUrl(),
                request.getRepositoryUrl());
    }

    public static class DownloadRequest {

        private String artifactUrl;
        private String localPath;

        public String getArtifactUrl() {
            return artifactUrl;
        }

        public void setArtifactUrl(String artifactUrl) {
            this.artifactUrl = artifactUrl;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }
    }

    @Path("/artifact/download")
    @Consumes("application/json")
    @POST
    public void download(DownloadRequest request) throws Exception {
        deployer.download(request.getArtifactUrl(), request.getLocalPath());
    }

    @Path("/{connection}/bundle/{url}")
    @POST
    public void deployBundle(@PathParam("connection") String connection, @PathParam("url") String url) throws Exception {
        deployer.installBundle(url, connection);
    }

    @Path("/{connection}/bundle/{id}")
    @DELETE
    public void undeployBundle(@PathParam("connection") String connection, @PathParam("id") String id) throws Exception {
        deployer.uninstallBundle(id, connection);
    }

    @Path("/{connection}/bundle/{id}/start")
    @GET
    public void startBundle(@PathParam("connection") String connection, @PathParam("id") String id) throws Exception {
        deployer.startBundle(id, connection);
    }

    @Path("/{connection}/bundle/{id}/stop")
    @GET
    public void stopBundle(@PathParam("connection") String connection, @PathParam("id") String id) throws Exception {
        deployer.stopBundle(id, connection);
    }

    @Path("/{connection}/bundle")
    @Produces("application/json")
    @GET
    public List<Bundle> listBundles(@PathParam("connection") String connection) throws Exception {
        return deployer.bundles(connection);
    }

    @Path("/{connection}/kar/{url}")
    @POST
    public void installKar(@PathParam("connection") String connection, @PathParam("url") String url) throws Exception {
        deployer.installKar(url, connection);
    }

    @Path("/{connection}/kar/{id}")
    @DELETE
    public void uninstallKar(@PathParam("connection") String connection, @PathParam("id") String id) throws Exception {
        deployer.uninstallKar(id, connection);
    }

    @Path("/{connection}/kar")
    @GET
    public List<String> listKars(@PathParam("connection") String connection) throws Exception {
        return deployer.kars(connection);
    }

    public static class FeatureAssembleRequest {

        private String groupId;
        private String artifactId;
        private String version;
        private String feature;
        private String repositoryUrl;
        private List<String> featureRepositories;
        private List<String> features;
        private List<String> bundles;
        private List<Config> configs;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

        public List<String> getFeatureRepositories() {
            return featureRepositories;
        }

        public void setFeatureRepositories(List<String> featureRepositories) {
            this.featureRepositories = featureRepositories;
        }

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }

        public List<String> getBundles() {
            return bundles;
        }

        public void setBundles(List<String> bundles) {
            this.bundles = bundles;
        }
        public List<Config> getConfigs() {
            return configs;
        }

        public void setConfigs(List<Config> configs) {
            this.configs = configs;
        }

    }

    @Path("/feature/assemble")
    @Consumes("application/json")
    @POST
    public void assembleFeature(FeatureAssembleRequest request) throws Exception {
        deployer.assembleFeature(request.getGroupId(),
                request.getArtifactId(),
                request.getVersion(),
                request.getRepositoryUrl(),
                request.getFeature(),
                request.getFeatureRepositories(),
                request.getFeatures(),
                request.getBundles(),
                request.getConfigs());
    }

    @Path("/{connection}/feature/repository/{url}")
    @POST
    public void addFeaturesRepository(@PathParam("connection") String connection, @PathParam("url") String url) throws Exception {
        deployer.addFeaturesRepository(url, connection);
    }

    @Path("/{connection}/feature/repository/{name}")
    @DELETE
    public void removeFeaturesRepository(@PathParam("connection") String connection, @PathParam("name") String name) throws Exception {
        deployer.removeFeaturesRepository(name, connection);
    }

    @Path("/{connection}/feature/repository")
    @Produces("application/json")
    @GET
    public List<FeaturesRepository> listFeaturesRepositories(@PathParam("connection") String connection) throws Exception {
        return deployer.featuresRepositories(connection);
    }

    @Path("/{connection}/feature/{feature}")
    @POST
    public void installFeature(@PathParam("connection") String connection, @PathParam("feature") String feature) throws Exception {
        deployer.installFeature(feature, connection);
    }

    @Path("/{connection}/feature/{feature}")
    @DELETE
    public void uninstallFeature(@PathParam("connection") String connection, @PathParam("feature") String feature) throws Exception {
        deployer.uninstallFeature(feature, connection);
    }

    @Path("/{connection}/feature")
    @Produces("application/json")
    @GET
    public List<Feature> listFeatures(@PathParam("connection") String connection) throws Exception {
        return deployer.features(connection);
    }

    @Path("/{connection}/config/{pid}")
    @POST
    public void createConfig(@PathParam("connection") String connection, @PathParam("pid") String pid) throws Exception {
        deployer.createConfig(pid, connection);
    }

    @Path("/{connection}/config/{pid}")
    @DELETE
    public void deleteConfig(@PathParam("connection") String connection, @PathParam("pid") String pid) throws Exception {
        deployer.deleteConfig(pid, connection);
    }

    @Path("/{connection}/config/{pid}/properties")
    @Produces("application/json")
    @GET
    public Map<String, String> getConfigProperties(@PathParam("connection") String connection, @PathParam("pid") String pid) throws Exception {
        return deployer.configProperties(pid, connection);
    }

    @Path("/{connection}/config/{pid}")
    @Consumes("application/json")
    @PUT
    public void updateConfig(@PathParam("connection") String connection, @PathParam("pid") String pid, Map<String, String> properties) throws Exception {
        Config config = new Config();
        config.setPid(pid);
        config.setProperties(properties);
        deployer.updateConfig(config, connection);
    }

    @Path("/{connection}/config/{pid}/{key}/{value}")
    @POST
    public void setConfigProperty(@PathParam("connection") String connection,
                                  @PathParam("pid") String pid,
                                  @PathParam("key") String key,
                                  @PathParam("value") String value) throws Exception {
        deployer.setConfigProperty(pid, key, value, connection);
    }

    @Path("/{connection}/config/{pid}/{key}")
    @GET
    public String getConfigProperty(@PathParam("connection") String connection,
                                    @PathParam("pid") String pid,
                                    @PathParam("key") String key) throws Exception {
        return deployer.configProperty(pid, key, connection);
    }

    @Path("/{connection}/config/{pid}/{key}")
    @DELETE
    public void deleteConfigProperty(@PathParam("connection") String connection,
                                     @PathParam("pid") String pid,
                                     @PathParam("key") String key) throws Exception {
        deployer.deleteConfigProperty(pid, key, connection);
    }

    @Path("/{connection}/config/{pid}/{key}/{value}")
    @PUT
    public void appendConfigProperty(@PathParam("connection") String connection,
                                     @PathParam("pid") String pid,
                                     @PathParam("key") String key,
                                     @PathParam("value") String value) throws Exception {
        deployer.appendConfigProperty(pid, key, value, connection);
    }

    @Path("/{connection}/cluster/{group}/feature/repository/{url}")
    @POST
    public void clusterAddFeaturesRepository(@PathParam("connection") String connection,
                                             @PathParam("group") String group,
                                             @PathParam("url") String url) throws Exception {
        deployer.clusterAddFeaturesRepository(url, group, connection);
    }

    @Path("/{connection}/cluster/{group}/feature/repository/{url}")
    @DELETE
    public void clusterRemoveFeaturesRepository(@PathParam("connection") String connection,
                                                @PathParam("group") String group,
                                                @PathParam("url") String url) throws Exception {
        deployer.clusterRemoveFeaturesRepository(url, group, connection);
    }

    @Path("/{connection}/cluster/{group}/feature/{feature}")
    @POST
    public void clusterInstallFeature(@PathParam("connection") String connection,
                                      @PathParam("group") String group,
                                      @PathParam("feature") String feature) throws Exception {
        deployer.clusterFeatureInstall(feature, group, connection);
    }

    @Path("/{connection}/cluster/{group}/feature/{feature}")
    @DELETE
    public void clusterUninstallFeature(@PathParam("connection") String connection,
                                        @PathParam("group") String group,
                                        @PathParam("feature") String feature) throws Exception {
        deployer.clusterFeatureUninstall(feature, group, connection);
    }

    @Path("/{connection}/cluster/nodes")
    @Produces("application/json")
    @GET
    public List<String> clusterNodes(@PathParam("connection") String connection) throws Exception {
        return deployer.clusterNodes(connection);
    }

    @Path("/{connection}/cluster/groups")
    @Produces("application/json")
    @GET
    public Map<String, List<String>> clusterGroups(@PathParam("connection") String connection) throws Exception {
        return deployer.clusterGroups(connection);
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

}
