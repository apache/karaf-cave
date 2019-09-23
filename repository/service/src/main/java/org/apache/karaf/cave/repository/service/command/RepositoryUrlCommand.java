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

import org.apache.karaf.cave.repository.RepositoryService;
import org.apache.karaf.cave.repository.service.command.completers.RepositoryNameCompleter;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "repository-url", description = "Get or set the repository URL")
public class RepositoryUrlCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "name", description = "The repository name", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String name;

    @Argument(index = 1, name = "url", description = "The new repository URL", required = false, multiValued = false)
    String url;

    @Override
    public Object execute() throws Exception {
        if (repositoryService.repository(name) == null) {
            System.err.println("Repository " + name + " doesn't exist");
            return null;
        }
        if (url != null) {
            repositoryService.changeUrl(name, url);
        }
        System.out.println(repositoryService.repository(name).getUrl());
        return null;
    }

}
