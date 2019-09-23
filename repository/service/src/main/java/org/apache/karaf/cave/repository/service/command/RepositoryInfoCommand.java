/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cave.repository.service.command;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;
import org.apache.karaf.cave.repository.service.command.completers.RepositoryNameCompleter;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "repository-info", description = "Provide all details about a repository")
public class RepositoryInfoCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "name", description = "The repository name", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String name;

    @Override
    public Object execute() throws Exception {
        Repository repository = repositoryService.repository(name);
        if (repository == null) {
            System.err.println("Repository " + name + " doesn't exist");
            return null;
        }
        System.out.println("Name: " + repository.getName());
        System.out.println("Location: " + repository.getLocation());
        System.out.println("URL: " + repository.getUrl());
        System.out.println("Proxy: " + ((repository.getProxy() != null) ? repository.getProxy() : ""));
        System.out.println("Mirror: " + repository.isMirror());
        System.out.println("Realm: " + ((repository.getRealm() != null) ? repository.getRealm() : ""));
        System.out.println("Download role: " + ((repository.getDownloadRole() != null) ? repository.getDownloadRole() : ""));
        System.out.println("Upload role: " + ((repository.getUploadRole() != null) ? repository.getUploadRole() : ""));
        System.out.println("Scheduling: " + ((repository.getScheduling() != null) ? repository.getScheduling() : ""));
        System.out.println("Scheduling Actions: " + ((repository.getScheduling() != null) ? repository.getSchedulingAction() : ""));
        System.out.println("Pool size: " + repository.getPoolSize());
        return null;
    }

}
