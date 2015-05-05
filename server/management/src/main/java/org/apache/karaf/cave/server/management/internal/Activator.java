package org.apache.karaf.cave.server.management.internal;

import java.util.Hashtable;

import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.framework.ServiceRegistration;

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

@Services(
        requires = { @RequireService(CaveRepositoryService.class) }
)
public class Activator extends BaseActivator {

    private volatile ServiceRegistration registration;

    @Override
    protected void doStart() throws Exception {
        CaveRepositoryService service = getTrackedService(CaveRepositoryService.class);
        CaveRepositoryMBeanImpl mbean = new CaveRepositoryMBeanImpl();
        mbean.setCaveRepositoryService(service);

        Hashtable<String, Object> props = new Hashtable<>();
        props.put("jmx.objectname", "org.apache.karaf.cave:type=repository,name=" + System.getProperty("karaf.name"));
        registration = this.bundleContext.registerService(getInterfaceNames(mbean), mbean, props);
    }

    @Override
    protected void doStop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        super.doStop();
    }
}
