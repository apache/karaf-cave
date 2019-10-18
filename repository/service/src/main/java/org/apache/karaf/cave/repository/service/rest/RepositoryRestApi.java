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
package org.apache.karaf.cave.repository.service.rest;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;

@Path("/")
public class RepositoryRestApi {

    private RepositoryService repositoryService;

    public RepositoryRestApi(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @POST
    @Path("/repositories")
    @Consumes("application/json")
    public void create(Repository repository) throws Exception {
        repositoryService.create(
                repository.getName(),
                repository.getLocation(),
                repository.getUrl(),
                repository.getProxy(),
                repository.isMirror(),
                repository.getRealm(),
                repository.getDownloadRole(),
                repository.getUploadRole(),
                repository.getScheduling(),
                repository.getSchedulingAction(),
                repository.getPoolSize()
        );
    }

    @GET
    @Path("/repositories")
    @Produces("application/json")
    public Collection<Repository> getRepositories() throws Exception {
        return repositoryService.repositories();
    }

    @GET
    @Path("/repositories/{name}")
    @Produces("application/json")
    public Repository getRepository(@PathParam(value = "name") String name) throws Exception {
        return repositoryService.repository(name);
    }

    @POST
    @Path("/repositories/{name}")
    public void create(@PathParam(value = "name") String name) throws Exception {
        repositoryService.create(name);
    }

    @DELETE
    @Path("/repositories/{name}")
    public void remove(@PathParam(value = "name") String name) throws Exception {
        repositoryService.remove(name);
    }

    @POST
    @Path("/repositories/{name}/purge")
    public void purge(@PathParam(value = "name") String name) throws Exception {
        repositoryService.purge(name);
    }

    @POST
    @Path("/repositories/{name}/artifact")
    public void addArtifact(@PathParam(value = "name") String name, @HeaderParam(value = "artifactUrl") String artifactUrl) throws Exception {
        repositoryService.addArtifact(artifactUrl, name);
    }

    @DELETE
    @Path("/repositories/{name}/artifact")
    public void deleteArtifact(@PathParam(value = "name") String name, @HeaderParam(value = "artifactUrl") String artifactUrl) throws Exception {
        repositoryService.deleteArtifact(artifactUrl, name);
    }

    @POST
    @Path("/repositories/{name}/bundle")
    public void updateBundleRepositoryDescription(@PathParam(value = "name") String name) throws Exception {
        repositoryService.updateBundleRepositoryDescriptor(name);
    }

}
