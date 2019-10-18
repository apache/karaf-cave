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
package org.apache.karaf.cave.repository.service.management;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

@Component(name = "org.apache.karaf.cave.repository.management", property = { "jmx.objectname=org.apache.karaf.cave:type=repository" })
public class RepositoryMBeanService extends StandardMBean implements RepositoryMBean {

    @Reference
    private RepositoryService repositoryService;

    public RepositoryMBeanService() throws NotCompliantMBeanException {
        super(RepositoryMBean.class);
    }

    @Override
    public TabularData getRepositories() throws Exception {
        CompositeType repositoryType = new CompositeType("Repository", "Cave Repository",
                new String[]{"name", "location", "url", "proxy", "mirror", "realm", "downloadRole", "uploadRole", "poolSize"},
                new String[]{"Name", "Location", "URL", "Proxy", "Mirror", "Realm", "Download Role", "Upload Role", "Pool Size"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER});
        TabularType tableType = new TabularType("Repositories", "Repositories", repositoryType, new String[]{"name"});
        TabularData table = new TabularDataSupport(tableType);
        for (Repository repository : repositoryService.repositories()) {
            CompositeData data = new CompositeDataSupport(repositoryType,
                    new String[]{"name", "location", "url", "proxy", "mirror", "realm", "downloadRole", "uploadRole", "poolSize"},
                    new Object[]{repository.getName(),
                            repository.getLocation(),
                            repository.getUrl(),
                            repository.getProxy(),
                            repository.isMirror(),
                            repository.getRealm(),
                            repository.getDownloadRole(),
                            repository.getUploadRole(),
                            repository.getPoolSize()});
            table.put(data);
        }
        return table;
    }

    @Override
    public void create(String name) throws Exception {
        repositoryService.create(name);
    }

    @Override
    public void create(String name, String location, String url, String proxy, boolean mirror, String realm, String downloadRole, String uploadRole, String scheduling, String schedulingAction, int poolSize) throws Exception {
        repositoryService.create(name, location, url, proxy, mirror, realm, downloadRole, uploadRole, scheduling, schedulingAction, poolSize);
    }

    @Override
    public void remove(String name, boolean purge) throws Exception {
        repositoryService.remove(name, purge);
    }

    @Override
    public void purge(String name) throws Exception {
        repositoryService.purge(name);
    }

    @Override
    public void changeLocation(String name, String location) throws Exception {
        repositoryService.changeLocation(name, location);
    }

    @Override
    public void changeUrl(String name, String url) throws Exception {
        repositoryService.changeUrl(name, url);
    }

    @Override
    public void changeProxy(String name, String proxy, boolean mirror) throws Exception {
        repositoryService.changeProxy(name, proxy, mirror);
    }

    @Override
    public void changeSecurity(String name, String realm, String downloadRole, String uploadRole) throws Exception {
        repositoryService.changeSecurity(name, realm, downloadRole, uploadRole);
    }

    @Override
    public void changeScheduling(String name, String scheduling, String actions) throws Exception {
        repositoryService.changeScheduling(name, scheduling, actions);
    }

    @Override
    public void copy(String source, String destination) throws Exception {
        repositoryService.copy(source, destination);
    }

    @Override
    public void addArtifact(String name, String artifactUrl) throws Exception {
        repositoryService.addArtifact(artifactUrl, name);
    }

    @Override
    public void deleteArtifact(String name, String artifactUrl) throws Exception {
        repositoryService.deleteArtifact(artifactUrl, name);
    }

    @Override
    public void updateBundleRepositoryDescriptor(String name) throws Exception {
        repositoryService.updateBundleRepositoryDescriptor(name);
    }
}
