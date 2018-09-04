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

import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.service.http.HttpService;

@Services(
        requires = {
                @RequireService(HttpService.class),
                @RequireService(CaveRepositoryService.class),
                @RequireService(Deployer.class)
        }
)
public class Activator extends BaseActivator {

    private HttpService httpService;
    private CaveRestServlet restServlet;

    @Override
    public void doStart() throws Exception {
        httpService = getTrackedService(HttpService.class);
        if (httpService == null) {
            return;
        }

        CaveRepositoryService repositoryService = getTrackedService(CaveRepositoryService.class);
        Deployer deployerService = getTrackedService(Deployer.class);

        RepositoryRest repositoryRest = new RepositoryRest(repositoryService);
        DeployerRest deployerRest = new DeployerRest(deployerService);
        restServlet = new CaveRestServlet(repositoryRest, deployerRest);
        httpService.registerServlet("/cave/rest", restServlet, null, null);
    }

    @Override
    public void doStop() {
        if (httpService != null) {
            if (restServlet != null) {
                httpService.unregister("/cave/rest");
            }
        }
        if (restServlet != null) {
            restServlet.destroy();
        }
        super.doStop();
    }

}
