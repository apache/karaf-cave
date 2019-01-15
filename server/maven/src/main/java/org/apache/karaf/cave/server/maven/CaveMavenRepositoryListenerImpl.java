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
package org.apache.karaf.cave.server.maven;

import org.apache.karaf.cave.server.api.CaveMavenRepositoryListener;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.osgi.service.http.HttpService;

import java.util.Hashtable;

public class CaveMavenRepositoryListenerImpl implements CaveMavenRepositoryListener {

    private int poolSize;
    private String realm;
    private String downloadRole;
    private String uploadRole;
    private HttpService httpService;

    public CaveMavenRepositoryListenerImpl(HttpService httpService, int poolSize, String realm, String downloadRole, String uploadRole) {
        this.httpService = httpService;
        this.poolSize = poolSize;
        this.realm = realm;
        this.downloadRole = downloadRole;
        this.uploadRole = uploadRole;
    }

    @Override
    public void addRepository(String name, String location) throws Exception {
        Hashtable<String, String> config = new Hashtable<>();
        config.put("defaultRepositories", "file:" + location + "@id=cave@snapshots");
        config.put("defaultLocalRepoAsRemote", "false");
        config.put("useFallbackRepositories", "false");
        config.put("localRepository", location);
        config.put("repositories", "file:" + location + "@id=" + name + "@snapshots");
        MavenResolver resolver = MavenResolvers.createMavenResolver(config, null);
        CaveMavenServlet servlet = new CaveMavenServlet(resolver, name, location, poolSize, realm, downloadRole, uploadRole);
        httpService.registerServlet("/cave/maven/repositories/" + name, servlet, null, null);
    }

    @Override
    public void removeRepository(String name) {
        httpService.unregister("/cave/maven/repositories/" + name);
    }

}
