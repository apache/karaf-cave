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

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.karaf.cave.server.api.CaveMavenRepositoryListener;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.Managed;
import org.apache.karaf.util.tracker.annotation.ProvideService;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;

@Services(
        requires = @RequireService(HttpService.class),
        provides = @ProvideService(CaveMavenRepositoryListener.class)
)
@Managed("org.apache.karaf.cave.server.maven")
public class Activator extends BaseActivator implements ManagedService {

    private HttpService httpService;
    private String alias;
    private MavenResolver resolver;
    private CaveMavenServlet servlet;

    @Override
    protected void doStart() throws Exception {
        httpService = getTrackedService(HttpService.class);
        if (httpService == null) {
            return;
        }

        String pid = getString("cave.maven.pid", "org.ops4j.pax.url.mvn");
        String alias = getString("cave.maven.alias", "/cave/maven/proxy");
        String realm = getString("cave.maven.realm", "karaf");
        String downloadRole = getString("cave.maven.downloadRole", null);
        String uploadRole = getString("cave.maven.uploadRole", null);
        int poolSize = getInt("cave.maven.poolSize", 8);
        Hashtable<String, String> config = new Hashtable<>();
        if (getConfiguration() != null) {
            for (Enumeration<String> e = getConfiguration().keys(); e.hasMoreElements();) {
                String key = e.nextElement();
                String val = getConfiguration().get(key).toString();
                config.put(key, val);
            }
        }
        this.resolver = MavenResolvers.createMavenResolver(config, pid);
        this.alias = alias;
        this.servlet = new CaveMavenServlet(this.resolver, poolSize, realm, downloadRole, uploadRole);
        this.httpService.registerServlet(this.alias, this.servlet, config, null);

        CaveMavenRepositoryListenerImpl repositoryListener = new CaveMavenRepositoryListenerImpl(httpService,
                poolSize, realm, downloadRole, uploadRole);
        register(CaveMavenRepositoryListener.class, repositoryListener);
    }

    @Override
    protected void doStop() {
        if (httpService != null) {
            try {
                httpService.unregister(alias);
                httpService.unregister("/cave/maven/repositories/test");
            } catch (Throwable t) {
                logger.debug("Exception caught while stopping", t);
            } finally {
                httpService = null;
            }
        }
        if (servlet != null) {
            try {
                servlet.destroy();
            } catch (Throwable t) {
                logger.debug("Exception caught while stopping", t);
            } finally {
                servlet = null;
            }
        }
        if (resolver != null) {
            try {
                resolver.close();
            } catch (Throwable t) {
                logger.debug("Exception caught while stopping", t);
            } finally {
                resolver = null;
            }
        }
        super.doStop();
    }
}
