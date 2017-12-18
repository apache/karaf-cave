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

import org.apache.karaf.cave.server.api.CaveFeatureGateway;
import org.apache.karaf.cave.server.management.CaveGatewayMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import java.util.List;

public class CaveGatewayMBeanImpl extends StandardMBean implements CaveGatewayMBean {

    private CaveFeatureGateway gateway;

    public CaveGatewayMBeanImpl() throws NotCompliantMBeanException {
        super(CaveGatewayMBean.class);
    }

    public void setGateway(CaveFeatureGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<String> list() throws Exception {
        return gateway.list();
    }

    @Override
    public void register(String repository) throws Exception {

    }

    @Override
    public void remove(String repository) throws Exception {

    }
}
