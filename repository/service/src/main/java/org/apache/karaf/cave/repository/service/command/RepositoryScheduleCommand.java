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
@Command(scope = "cave", name = "repository-schedule", description = "Get or set the scheduling and action for a repository")
public class RepositoryScheduleCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "name", description = "The repository name", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String name;

    @Argument(index = 1, name = "scheduling", description = "Set the scheduling (cron:* or at:*)", required = false, multiValued = false)
    String scheduling;

    @Argument(index = 2, name = "action", description = "Set the action at schedule (DELETE, PURGE, COPY)", required = false, multiValued = false)
    String action;

    @Override
    public Object execute() throws Exception {
        if (repositoryService.repository(name) == null) {
            System.err.println("Repository " + name + " doesn't exist");
            return null;
        }
        if (scheduling != null && action != null) {
            repositoryService.changeScheduling(name, scheduling, action);
        }
        System.out.println("perform " + repositoryService.repository(name).getSchedulingAction() + " at " + repositoryService.repository(name).getScheduling());
        return null;
    }

}
