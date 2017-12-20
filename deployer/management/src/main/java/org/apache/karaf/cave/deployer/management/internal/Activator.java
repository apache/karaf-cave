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
package org.apache.karaf.cave.deployer.management.internal;

import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

@Services(
        requires = {
                @RequireService(Deployer.class)
        }
)
public class Activator extends BaseActivator {

    private volatile ServiceRegistration mbeanRegistration;

    @Override
    protected void doStart() throws Exception {
        Deployer deployer = getTrackedService(Deployer.class);
        CaveDeployerMBeanImpl mbean = new CaveDeployerMBeanImpl();
        mbean.setDeployer(deployer);

        Hashtable<String, Object> props = new Hashtable<>();
        props.put("jmx.objectname", "org.apache.karaf.cave:type=deployer,name=" + System.getProperty("karaf.name"));
        mbeanRegistration = this.bundleContext.registerService(getInterfaceNames(mbean), mbean, props);
    }

    @Override
    protected void doStop() {
        if (mbeanRegistration != null) {
            mbeanRegistration.unregister();
            mbeanRegistration = null;
        }
        super.doStop();
    }

}
