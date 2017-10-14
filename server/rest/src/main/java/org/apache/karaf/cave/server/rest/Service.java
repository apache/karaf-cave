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
package org.apache.karaf.cave.server.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;

/**
 * Service to manipulate a Cave repository.
 */
@Path("/")
public class Service {

    private final CaveRepositoryService service;

    public Service(CaveRepositoryService service) {
        this.service = service;
    }

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository
     * @param scan if true, the repository is scanned at creation time, and the repository metadata are created.
     * @return the Cave repository.
     * @throws Exception in case of creation failure.
     */
    @POST
    @Path("/repositories")
    @Produces("application/json")
    public Repository create(@QueryParam(value = "name") String name, @QueryParam(value = "scan") boolean scan) throws Exception {
        return new Repository(service.create(name, scan));
    }

    /**
     * Create a Cave repository.
     *
     * @param name the name of the repository.
     * @param location the storage location of the repository.
     * @param scan if true, the repository is scanned at creation time, and the repository metadata are created.
     * @return the Cave repository.
     * @throws Exception in case of creation failure.
     */
    @POST
    @Path("/repositories")
    @Produces("application/json")
    public Repository create(@QueryParam(value = "name") String name, @QueryParam(value = "location") String location, @QueryParam(value = "scan") boolean scan) throws Exception {
        return new Repository(service.create(name, location, scan));
    }

    /**
     * Uninstall a Cave repository from the repository service.
     *
     * @param name the name of the repository.
     * @throws Exception in case of uninstall failure.
     */
    @POST
    @Path("/uninstall")
    public void uninstall(@QueryParam(value = "name") String name) throws Exception {
        service.uninstall(name);
    }

    /**
     * Remove a Cave repository from the repositories registry.
     *
     * @param name the name of the repository.
     * @throws Exception in case of remove failure.
     */
    @POST
    @Path("/remove")
    public void remove(@QueryParam(value = "name") String name) throws Exception {
        service.remove(name);
    }

    /**
     * Destroy a Cave repository, including the storage.
     *
     * @param name the name of the repository.
     * @throws Exception incase of remove failure.
     */
    @DELETE
    @Path("/repositories")
    public void destroy(@QueryParam(value = "name") String name) throws Exception {
        service.destroy(name);
    }

    /**
     * Install a Cave repository into the repository service.
     *
     * @param name the name of the Cave repository.
     * @throws Exception in case of registration failure.
     */
    @POST
    @Path("/install")
    public void install(@QueryParam(value = "name") String name) throws Exception {
        service.install(name);
    }

    /**
     * Get the list of all Cave repositories.
     *
     * @return the Cave repositories.
     */
    @GET
    @Path("/repositories")
    @Produces("application/json")
    public Repository[] getRepositories() {
        CaveRepository[] repositories = service.getRepositories();
        Repository[] repos = new Repository[repositories.length];
        for (int i = 0; i < repositories.length; i++) {
            repos[i] = new Repository(repositories[i]);
        }
        
        return repos;
    }

    /**
     * Get a Cave repository identified by the given name.
     *
     * @param name the name of the Cave repository.
     * @return the Cave repository
     */
    @GET
    @Path("/repositories/{name}")
    @Produces("application/json")
    public Repository getRepository(@PathParam("name") String name) {
        CaveRepository repository = service.getRepository(name);
        if (repository != null) {
            return new Repository(repository);
        } else {
            return null;
        }
    }

}
