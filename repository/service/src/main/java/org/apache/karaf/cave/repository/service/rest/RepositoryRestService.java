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

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.DestinationRegistryImpl;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.karaf.cave.repository.RepositoryService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import java.util.HashMap;
import java.util.Map;

@Component(name = "org.apache.karaf.cave.repository.rest")
public class RepositoryRestService {

    @Reference
    private RepositoryService repositoryService;

    @Reference
    private HttpService httpService;

    @Activate
    public void activate(ComponentContext componentContext) throws Exception {
        RepositoryRestApi restApi = new RepositoryRestApi(repositoryService);

        Map<Class<?>, Object> extensions = new HashMap<>();
        DestinationRegistry destinationRegistry = new DestinationRegistryImpl();
        HTTPTransportFactory httpTransportFactory = new HTTPTransportFactory(destinationRegistry);
        extensions.put(HTTPTransportFactory.class, httpTransportFactory);
        extensions.put(DestinationRegistry.class, destinationRegistry);
        Bus bus = new ExtensionManagerBus(extensions, null, getClass().getClassLoader());
        org.apache.cxf.transport.DestinationFactoryManager destinationFactoryManager = bus.getExtension(org.apache.cxf.transport.DestinationFactoryManager.class);
        for (String url : HTTPTransportFactory.DEFAULT_NAMESPACES) {
            destinationFactoryManager.registerDestinationFactory(url, httpTransportFactory);
        }

        RepositoryRestServlet restServlet = new RepositoryRestServlet(restApi, destinationRegistry, bus);
        httpService.registerServlet("/cave/repository/api", restServlet, null, null);
    }

    @Deactivate
    public void deactivate() throws Exception {
        httpService.unregister("/cave/repository/api");
    }

}
