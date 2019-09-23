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

import javax.management.openmbean.TabularData;

public interface RepositoryMBean {

    TabularData getRepositories() throws Exception;

    void create(String name) throws Exception;
    void create(String name, String location, String url, String proxy, boolean mirror, String realm, String downloadRole, String uploadRole, String scheduling, String schedulingAction, int poolSize) throws Exception;
    void remove(String name, boolean purge) throws Exception;
    void purge(String name) throws Exception;
    void changeLocation(String name, String location) throws Exception;
    void changeUrl(String name, String url) throws Exception;
    void changeProxy(String name, String proxy, boolean mirror) throws Exception;
    void changeSecurity(String name, String realm, String downloadRole, String uploadRole) throws Exception;
    void changeScheduling(String name, String scheduling, String actions) throws Exception;
    void copy(String source, String destination) throws Exception;
    void addArtifact(String name, String artifactUrl) throws Exception;
    void deleteArtifact(String name, String artifactUrl) throws Exception;
    void updateBundleRepositoryDescriptor(String name) throws Exception;

}
