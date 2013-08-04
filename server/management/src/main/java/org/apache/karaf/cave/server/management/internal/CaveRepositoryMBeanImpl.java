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

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.cave.server.management.CaveRepositoryMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.*;

/**
 * Implementation of the Cave repository MBean.
 */
public class CaveRepositoryMBeanImpl extends StandardMBean implements CaveRepositoryMBean {

    private CaveRepositoryService caveRepositoryService;

    public CaveRepositoryMBeanImpl() throws NotCompliantMBeanException {
        super(CaveRepositoryMBean.class);
    }

    public CaveRepositoryService getCaveRepositoryService() {
        return this.caveRepositoryService;
    }

    public void setCaveRepositoryService(CaveRepositoryService caveRepositoryService) {
        this.caveRepositoryService = caveRepositoryService;
    }

    public TabularData getCaveRepositories() throws Exception {
        CaveRepository[] caveRepositories = caveRepositoryService.getRepositories();

        CompositeType caveRepositoryType = new CompositeType("Repository", "Karaf Cave repository",
                new String[]{"name", "location"},
                new String[]{"Name of the Cave repository", "Location of the Cave repository storage"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING });

        TabularType tableType = new TabularType("Repositories", "Table of all Karaf Cave repositories", caveRepositoryType,
                new String[]{ "name" });

        TabularData table = new TabularDataSupport(tableType);

        for (int i = 0; i < caveRepositories.length; i++) {
            CompositeData data = new CompositeDataSupport(caveRepositoryType,
                    new String[]{ "name", "location" },
                    new Object[]{ caveRepositories[i].getName(), caveRepositories[i].getLocation() });
            table.put(data);
        }

        return table;
    }

    public void createRepository(String name) throws Exception {
        caveRepositoryService.createRepository(name, true);
    }

    public void removeRepository(String name) throws Exception {
        caveRepositoryService.uninstall(name);
    }

}
