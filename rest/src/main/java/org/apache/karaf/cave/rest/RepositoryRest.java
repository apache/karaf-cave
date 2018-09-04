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
package org.apache.karaf.cave.rest;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Service to manipulate a Cave repository.
 */
@Path("/")
public class RepositoryRest {

    private final CaveRepositoryService service;

    public RepositoryRest(CaveRepositoryService service) {
        this.service = service;
    }

    class Repository {
        private String name;
        private String location;
        private String realm;
        private String downloadRole;
        private String uploadRole;
        private boolean scan;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getDownloadRole() {
            return downloadRole;
        }

        public void setDownloadRole(String downloadRole) {
            this.downloadRole = downloadRole;
        }

        public String getUploadRole() {
            return uploadRole;
        }

        public void setUploadRole(String uploadRole) {
            this.uploadRole = uploadRole;
        }

        public boolean isScan() {
            return scan;
        }

        public void setScan(boolean scan) {
            this.scan = scan;
        }
    }

    @POST
    @Path("/repositories")
    @Consumes("application/json")
    public void create(Repository repository) throws Exception {
        service.create(repository.getName(),
                repository.getLocation(),
                repository.getRealm(),
                repository.getDownloadRole(),
                repository.getUploadRole(),
                repository.isScan());
    }

    @POST
    @Path("/repositories/{name}/uninstall")
    public void uninstall(@PathParam(value = "name") String name) throws Exception {
        service.uninstall(name);
    }

    @POST
    @Path("/repositories/{name}/remove")
    public void remove(@PathParam(value = "name") String name) throws Exception {
        service.remove(name);
    }

    @DELETE
    @Path("/repositories/{name}")
    public void destroy(@PathParam(value = "name") String name) throws Exception {
        service.destroy(name);
    }

    @POST
    @Path("/repositories/{name}/install")
    public void install(@PathParam(value = "name") String name) throws Exception {
        service.install(name);
    }

    @GET
    @Path("/repositories")
    @Produces("application/json")
    public List<Repository> getRepositories() {
        CaveRepository[] repositories = service.getRepositories();
        List<Repository> repos = new ArrayList<>();
        for (int i = 0; i < repositories.length; i++) {
            Repository repo = new Repository();
            repo.setName(repositories[i].getName());
            repo.setLocation(repositories[i].getLocation());
            repo.setRealm(repositories[i].getRealm());
            repo.setDownloadRole(repositories[i].getDownloadRole());
            repo.setUploadRole(repositories[i].getUploadRole());
            repos.add(repo);
        }
        return repos;
    }

    @GET
    @Path("/repositories/{name}")
    @Produces("application/json")
    public Repository getRepository(@PathParam("name") String name) {
        CaveRepository repository = service.getRepository(name);
        if (repository != null) {
            Repository repo = new Repository();
            repo.setName(repository.getName());
            repo.setLocation(repository.getLocation());
            repo.setRealm(repository.getRealm());
            repo.setDownloadRole(repository.getDownloadRole());
            repo.setUploadRole(repository.getUploadRole());
            return repo;
        } else {
            return null;
        }
    }

}
