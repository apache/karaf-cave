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
package org.apache.karaf.cave.deployer.service.rest;

import org.apache.karaf.cave.deployer.Bundle;
import org.apache.karaf.cave.deployer.Connection;
import org.apache.karaf.cave.deployer.DeployerService;
import org.apache.karaf.cave.deployer.Feature;
import org.apache.karaf.cave.deployer.FeaturesRepository;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;

@Path("/")
public class DeployerRestApi {

    private DeployerService deployerService;

    public DeployerRestApi(DeployerService deployerService) {
        this.deployerService = deployerService;
    }

    @GET
    @Path("/connections")
    @Produces("application/json")
    List<Connection> getConnections() throws Exception {
        return deployerService.connections();
    }

    @POST
    @Path("/connections")
    @Consumes("application/json")
    public void addConnection(Connection connection) throws Exception {
        deployerService.registerConnection(connection);
    }

    @DELETE
    @Path("/connections/{connection}")
    public void removeConnection(@PathParam(value = "connection") String connection) throws Exception {
        deployerService.deleteConnection(connection);
    }

    @POST
    @Path("/explode")
    @Produces("application/json")
    public List<String> explode(@HeaderParam(value = "url") String url, @HeaderParam(value = "repository") String repository) throws Exception {
        return deployerService.explode(url, repository);
    }

    @POST
    @Path("/extract")
    public void extract(@HeaderParam(value = "url") String url, @HeaderParam(value = "directory") String directory) throws Exception {
        deployerService.extract(url, directory);
    }

    @POST
    @Path("/download")
    public void download(@HeaderParam(value = "artifact") String artifact, @HeaderParam(value = "directory") String directory) throws Exception {
        deployerService.download(artifact, directory);
    }

    @POST
    @Path("/upload")
    public void upload(@HeaderParam(value = "groupId") String groupId, @HeaderParam(value = "artifactId") String artifactId, @HeaderParam(value = "version") String version, @HeaderParam(value = "artifactUrl") String artifactUrl, @HeaderParam(value = "repositoryUrl") String repositoryUrl) throws Exception {
        deployerService.upload(groupId, artifactId, version, artifactUrl, repositoryUrl);
    }

    @POST
    @Path("/connections/{connection}/bundles")
    public void installBundle(@HeaderParam(value = "artifactUrl") String artifactUrl, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.installBundle(artifactUrl, connection);
    }

    @DELETE
    @Path("/connections/{connection}/bundles/{id}")
    public void uninstallBundle(@PathParam(value = "id") String id, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.uninstallBundle(id, connection);
    }

    @POST
    @Path("/connections/{connection}/bundles/{id}/start")
    public void startBundle(@PathParam(value = "id") String id, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.startBundle(id, connection);
    }

    @POST
    @Path("/connections/{connection}/bundles/{id}/stop")
    public void stopBundle(@PathParam(value = "id") String id, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.stopBundle(id, connection);
    }

    @GET
    @Path("/connections/{connection}/bundles")
    @Produces("application/json")
    public List<Bundle> listBundles(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.bundles(connection);
    }

    @POST
    @Path("/connections/{connection}/kars")
    public void installKar(@HeaderParam(value = "artifactUrl") String artifactUrl, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.installKar(artifactUrl, connection);
    }

    @DELETE
    @Path("/connections/{connection}/kars/{id}")
    public void uninstallKar(@PathParam(value = "id") String id, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.uninstallKar(id, connection);
    }

    @GET
    @Path("/connections/{connection}/kars")
    public List<String> listKars(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.kars(connection);
    }

    @GET
    @Path("/features/repository")
    public List<Feature> providedFeatures(@HeaderParam(value = "featuresRepositoryUrl") String featuresRepositoryUrl) throws Exception {
        return deployerService.providedFeatures(featuresRepositoryUrl);
    }

    @GET
    @Path("/connections/{connection}/features/repositories")
    @Produces("application/json")
    public List<FeaturesRepository> listFeaturesRepositories(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.featuresRepositories(connection);
    }

    @POST
    @Path("/connections/{connection}/features/repositories")
    public void addFeaturesRepository(@HeaderParam(value = "artifactUrl") String artifactUrl, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.addFeaturesRepository(artifactUrl, connection);
    }

    @DELETE
    @Path("/connections/{connection}/features/repositories")
    public void removeFeaturesRepository(@HeaderParam(value = "artifactUrl") String artifactUrl, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.removeFeaturesRepository(artifactUrl, connection);
    }

    @GET
    @Path("/connections/{connection}/features")
    @Produces("application/json")
    public List<Feature> listFeatures(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.features(connection);
    }

    @POST
    @Path("/connections/{connection}/features/{feature}")
    public void installFeature(@PathParam(value = "feature") String feature, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.installFeature(feature, connection);
    }

    @DELETE
    @Path("/connections/{connection}/features/{feature}")
    public void uninstallFeature(@PathParam(value = "feature") String feature, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.uninstallFeature(feature, connection);
    }

    @GET
    @Path("/connections/{connection}/configurations")
    @Produces("application/json")
    public List<String> getConfigs(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.configs(connection);
    }

    @POST
    @Path("/connections/{connection}/configurations/{pid}")
    public void createconfig(@PathParam(value = "pid") String pid, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.createConfig(pid, connection);
    }

    @POST
    @Path("/connections/{connection}/configurations/factories/{factoryPid}")
    public void createConfigFactory(@PathParam(value = "factoryPid") String pid, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.createConfigurationFactory(pid, connection);
    }

    @GET
    @Path("/connections/{connection}/configurations/{pid}/properties")
    @Produces("application/json")
    public Map<String, String> getConfigProperties(@PathParam(value = "pid")String pid, @PathParam(value = "connection") String connection) throws Exception {
        return deployerService.configProperties(pid, connection);
    }

    @DELETE
    @Path("/connections/{connection}/configurations/{pid}")
    public void deleteConfig(@PathParam(value = "pid") String pid, @PathParam(value = "connection") String connection) throws Exception {
        deployerService.deleteConfig(pid, connection);
    }

    @GET
    @Path("/connections/{connection}/cluster/nodes")
    @Produces("application/json")
    public List<String> listClusterNodes(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.clusterNodes(connection);
    }

    @GET
    @Path("/connections/{connection}/cluster/groups")
    @Produces("application/json")
    public Map<String, List<String>> listClusterGroups(@PathParam(value = "connection") String connection) throws Exception {
        return deployerService.clusterGroups(connection);
    }

    @POST
    @Path("/connections/{connection}/cluster/groups/{group}/features/repositories")
    public void clusterAddFeaturesRepository(@PathParam(value = "connection") String connection, @PathParam(value = "group") String clusterGroup, @HeaderParam(value = "url") String repositoryUrl) throws Exception {
        deployerService.clusterAddFeaturesRepository(repositoryUrl, clusterGroup, connection);
    }

    @DELETE
    @Path("/connections/{connection}/cluster/groups/{group}/features/repositories")
    public void clusterRemoveFeaturesRepository(@PathParam(value = "connection") String connection, @PathParam(value = "group") String clusterGroup, @HeaderParam(value = "url") String repositoryUrl) throws Exception {
        deployerService.clusterRemoveFeaturesRepository(repositoryUrl, clusterGroup, connection);
    }

    @POST
    @Path("/connections/{connection}/cluster/groups/{group}/features/{feature}")
    public void clusterInstallFeature(@PathParam(value = "connection") String connection, @PathParam(value = "group") String clusterGroup, @PathParam(value = "feature") String feature) throws Exception {
        deployerService.clusterFeatureInstall(feature, clusterGroup, connection);
    }

    @DELETE
    @Path("/connections/{connection}/cluster/groups/{group}/features/{feature}")
    public void clusterUninstallFeature(@PathParam(value = "connection") String connection, @PathParam(value = "group") String clusterGroup, @PathParam(value = "feature") String feature) throws Exception {
        deployerService.clusterFeatureUninstall(feature, clusterGroup, connection);
    }

}
