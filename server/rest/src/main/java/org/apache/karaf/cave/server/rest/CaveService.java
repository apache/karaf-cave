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

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.karaf.cave.server.backend.api.CaveRepository;
import org.apache.karaf.cave.server.backend.api.CaveRepositoryService;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cave REST service
 */
@Path("/")
public class CaveService {

    private RepositoryAdmin repositoryAdmin;
    private CaveRepositoryService caveRepositoryService;

    public CaveService() { }

    public RepositoryAdmin getRepositoryAdmin() {
        return this.repositoryAdmin;
    }

    public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }

    public CaveRepositoryService getCaveRepositoryService() {
        return this.caveRepositoryService;
    }

    public void setCaveRepositoryService(CaveRepositoryService caveRepositoryService) {
        this.caveRepositoryService = caveRepositoryService;
    }

    @GET
    @Path("cave-repositories")
    @Consumes("application/xml")
    @Produces("application/xml")
    public CaveRepositoriesWrapper getCaveRepositories() {
        List<CaveRepositoryWrapper> repositories = new ArrayList<CaveRepositoryWrapper>();
        for (CaveRepository caveRepository : caveRepositoryService.getRepositories()) {
            repositories.add(new CaveRepositoryWrapper(caveRepository.getName(), caveRepository.getLocation()));
        }
        return new CaveRepositoriesWrapper(repositories);
    }

    @GET
    @Path("cave-repositories/{caveRepoName}")
    @Consumes("application/xml")
    @Produces("application/xml")
    public CaveRepository getCaveRepository(@PathParam("caveRepoName") String caveRepoName) {
        return caveRepositoryService.getRepository(caveRepoName);
    }

    @POST
    @Path("cave-repositories")
    @Consumes("application/xml")
    @Produces("application/xml")
    public void addCaveRepository(@PathParam("caveRepoName") String caveRepoName, @PathParam("caveRepoLocation") String caveRepoLocation) throws Exception {
        caveRepositoryService.createRepository(caveRepoName, caveRepoLocation, false);
    }

    /**
     * A wrapper to the Karaf Cave repository to exchange with the REST client.
     */
    public static class CaveRepositoriesWrapper {

        private final List<CaveRepositoryWrapper> repositories;

        public CaveRepositoriesWrapper(List<CaveRepositoryWrapper> repositories) {
            this.repositories = repositories;
        }

        public List<CaveRepositoryWrapper> getRepositories() {
            return repositories;
        }

    }

    public static class CaveRepositoryWrapper {

        private final String name;
        private final String location;

        public CaveRepositoryWrapper(String name, String location) {
            this.name = name;
            this.location = location;
        }

        public String getName() {
            return this.name;
        }

        public String getLocation() {
            return this.location;
        }

    }

}
