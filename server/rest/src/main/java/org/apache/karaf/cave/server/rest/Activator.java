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

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;


@Services(
        requires = { @RequireService(CaveRepositoryService.class) }
)
public class Activator extends BaseActivator {

    private Server server;

    @Override
    protected void doStart() throws Exception {
        Service service = new Service(getTrackedService(CaveRepositoryService.class));
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(Service.class, Repository.class);
        sf.setResourceProvider(Service.class, new SingletonResourceProvider(service));
        sf.setAddress("/cave/rest");
        server = sf.create();
    }

    @Override
    protected void doStop() {
        if (server != null) {
            server.destroy();
            server = null;
        }
        super.doStop();
    }

}
