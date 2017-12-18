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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.cave.server.management.CaveRepositoryMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.*;
import java.net.URL;

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

    public void createRepository(String name, String location, boolean generate, boolean install) throws Exception {
        if (getCaveRepositoryService().getRepository(name) != null) {
            throw new IllegalArgumentException("Cave repository " + name + " already exists");
        }
        if (location != null) {
            getCaveRepositoryService().create(name, location, false);
        } else {
            getCaveRepositoryService().create(name, false);
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(name);
        if (generate) {
            caveRepository.scan();
        }
        if (install) {
            getCaveRepositoryService().install(name);
        }
    }

    public void destroyRepository(String name) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        caveRepositoryService.destroy(name);
    }

    public void installRepository(String name) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        caveRepositoryService.install(name);
    }

    public void uninstallRepository(String name) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        caveRepositoryService.uninstall(name);
    }

    public void populateRepository(String name, String url, boolean generate, String filter, String properties) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        CaveRepository repository = getCaveRepositoryService().getRepository(name);
        repository.populate(new URL(url), filter, Utils.loadProperties(properties), generate);
        if (generate) {
            getCaveRepositoryService().install(name);
        }
    }

    public void proxyRepository(String name, String url, boolean generate, String filter, String properties) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        CaveRepository repository = getCaveRepositoryService().getRepository(name);
        repository.proxy(new URL(url), filter, Utils.loadProperties(properties));
        if (generate) {
            getCaveRepositoryService().install(name);
        }
    }

    public void updateRepository(String name) throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            throw new IllegalArgumentException("Cave repository " + name + " doesn't exist");
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(name);
        caveRepository.scan();
    }

    public void uploadArtifact(String repository, String artifactUrl, boolean generate) throws Exception {
        if (getCaveRepositoryService().getRepository(repository) == null) {
            throw new IllegalArgumentException("Cave repository " + repository + " doesn't exist");
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(repository);
        caveRepository.upload(new URL(artifactUrl));
        if (generate) {
            getCaveRepositoryService().install(repository);
        }
    }

    static class Utils {

        /**
         * Returns the <code>Properties</code> object as represented by the
         * property list (key and element pairs) in the file path
         * at the given <code<>propertiesFile</code>.
         *
         * @param   propertiesFile a Properties file containing key-element pairs.
         * @return  a <code>Properties</code> object containing the properties read from the given file.
         * @throws  IOException if an error occurred when reading from the input stream.
         */
        public static Properties loadProperties (String propertiesFile) throws IOException {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            return properties;
        }
    }
}
