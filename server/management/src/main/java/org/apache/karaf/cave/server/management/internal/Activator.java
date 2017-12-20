/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cave.server.management.internal;

import java.util.Hashtable;

import org.apache.karaf.cave.server.api.CaveFeatureGateway;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.framework.ServiceRegistration;

@Services(
        requires = {
                @RequireService(CaveRepositoryService.class),
                @RequireService(CaveFeatureGateway.class)
        }
)
public class Activator extends BaseActivator {

    private volatile ServiceRegistration repositoryRegistration;
    private volatile ServiceRegistration gatewayRegistration;

    @Override
    protected void doStart() throws Exception {
        CaveRepositoryService repositoryService = getTrackedService(CaveRepositoryService.class);
        CaveRepositoryMBeanImpl repositoryMBean = new CaveRepositoryMBeanImpl();
        repositoryMBean.setCaveRepositoryService(repositoryService);

        Hashtable<String, Object> repositoryProps = new Hashtable<>();
        repositoryProps.put("jmx.objectname", "org.apache.karaf.cave:type=repository,name=" + System.getProperty("karaf.name"));
        repositoryRegistration = this.bundleContext.registerService(getInterfaceNames(repositoryMBean), repositoryMBean, repositoryProps);

        CaveFeatureGateway gatewayService = getTrackedService(CaveFeatureGateway.class);
        CaveGatewayMBeanImpl gatewayMBean = new CaveGatewayMBeanImpl();
        gatewayMBean.setGateway(gatewayService);

        Hashtable<String, Object> gatewayProps = new Hashtable<>();
        gatewayProps.put("jmx.objectname", "org.apache.karaf.cave:type=gateway,name=" + System.getProperty("karaf.name"));
        gatewayRegistration = this.bundleContext.registerService(getInterfaceNames(gatewayMBean), gatewayMBean, gatewayProps);
    }

    @Override
    protected void doStop() {
        if (repositoryRegistration != null) {
            repositoryRegistration.unregister();
            repositoryRegistration = null;
        }
        if (gatewayRegistration != null) {
            gatewayRegistration.unregister();
            gatewayRegistration = null;
        }
        super.doStop();
    }
}
