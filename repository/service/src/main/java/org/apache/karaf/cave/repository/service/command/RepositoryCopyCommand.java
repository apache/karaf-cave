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
@Command(scope = "cave", name = "repository-copy", description = "Copy a repository storage to another one")
public class RepositoryCopyCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "source", description = "Name of the source repository", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String source;

    @Argument(index = 1, name = "destination", description = "Name of the destination repository", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String destination;

    @Override
    public Object execute() throws Exception {
        if (repositoryService.repository(source) == null) {
            System.err.println("Source repository " + source + " doesn't exist");
            return null;
        }
        if (repositoryService.repository(destination) == null) {
            System.err.println("Destination repository " + destination + " doesn't exist");
            return null;
        }
        repositoryService.copy(source, destination);
        return null;
    }

}
