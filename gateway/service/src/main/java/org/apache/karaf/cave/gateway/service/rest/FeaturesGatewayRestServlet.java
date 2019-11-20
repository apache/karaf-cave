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
package org.apache.karaf.cave.gateway.service.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class FeaturesGatewayRestServlet extends CXFNonSpringServlet {

    private FeaturesGatewayRestApi restApi;

    private Server server;

    public FeaturesGatewayRestServlet(FeaturesGatewayRestApi restApi, DestinationRegistry destinationRegistry, Bus bus) {
        super(destinationRegistry, false);
        this.restApi = restApi;
        this.setBus(bus);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (restApi != null) {
            JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
            bean.setAddress("/");
            bean.setBus(getBus());
            bean.setProvider(new JacksonJsonProvider());
            bean.setServiceBean(restApi);
            server = bean.create();
        }
    }

    @Override
    public void destroy() {
        if (server != null) {
            server.destroy();
        }
        super.destroy();
    }

}
