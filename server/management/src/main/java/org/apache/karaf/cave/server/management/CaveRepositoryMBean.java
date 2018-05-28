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
package org.apache.karaf.cave.server.management;

import javax.management.openmbean.TabularData;

/**
 * Cave repository MBean to manage Cave repositories.
 */
public interface CaveRepositoryMBean {

    TabularData getCaveRepositories() throws Exception;

    void createRepository(String name, String location, String realm, String downloadRole, String uploadRole, boolean generate, boolean install) throws Exception;
    void destroyRepository(String name) throws Exception;
    void installRepository(String name) throws Exception;
    void uninstallRepository(String name) throws Exception;
    void populateRepository(String name, String url, boolean generate, String filter, String properties) throws Exception;
    void proxyRepository(String name, String url, boolean generate, String filter, String properties) throws Exception;
    void updateRepository(String name) throws Exception;
    void uploadArtifact(String repository, String artifactUrl, boolean generate) throws Exception;

}
