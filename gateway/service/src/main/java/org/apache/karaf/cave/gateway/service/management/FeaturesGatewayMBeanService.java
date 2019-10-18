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
package org.apache.karaf.cave.gateway.service.management;

import org.apache.karaf.cave.gateway.FeaturesGatewayService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import java.util.List;

@Component(name = "org.apache.karaf.cave.gateway.management", property = { "jmx.objectname=org.apache.karaf.cave:type=gateway" })
public class FeaturesGatewayMBeanService extends StandardMBean implements FeaturesGatewayMBean {

    @Reference
    private FeaturesGatewayService featuresGatewayService;

    public FeaturesGatewayMBeanService() throws NotCompliantMBeanException {
        super(FeaturesGatewayMBean.class);
    }

    @Override
    public List<String> getRepositories() throws Exception {
        return featuresGatewayService.list();
    }

    @Override
    public void register(String url) throws Exception {
        featuresGatewayService.register(url);
    }

    @Override
    public void remove(String url) throws Exception {
        featuresGatewayService.remove(url);
    }
}
