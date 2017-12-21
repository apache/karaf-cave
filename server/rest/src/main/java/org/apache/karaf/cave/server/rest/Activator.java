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

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.Managed;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;


@Services(
        requires = { @RequireService(CaveRepositoryService.class),
                     @RequireService(HttpService.class) }
)
@Managed("org.apache.karaf.cave.server.rest")
public class Activator extends BaseActivator implements ManagedService {

    private HttpService httpService;
    private String alias;
    private CXFNonSpringJaxrsServlet servlet;

    @Override
    protected void doStart() throws Exception {
        httpService = getTrackedService(HttpService.class);
        if (httpService == null) {
            return;
        }

        Service service = new Service(getTrackedService(CaveRepositoryService.class));

        String alias = getString("cave.rest.alias", "/cave/rest");
        Hashtable<String, String> config = new Hashtable<>();
        if (getConfiguration() != null) {
            for (Enumeration<String> e = getConfiguration().keys(); e.hasMoreElements();) {
                String key = e.nextElement();
                String val = getConfiguration().get(key).toString();
                config.put(key, val);
            }
        }
        this.alias = alias;
        this.servlet = new CaveRestServlet(service);
        this.httpService.registerServlet(this.alias, this.servlet, config, null);
    }

    @Override
    protected void doStop() {
        if (httpService != null) {
            httpService.unregister(alias);
        }
        if (this.servlet != null) {
            this.servlet.destroy();
        }
        super.doStop();
    }

}
