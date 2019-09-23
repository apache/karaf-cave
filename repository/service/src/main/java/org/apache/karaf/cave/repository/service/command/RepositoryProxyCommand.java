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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "repository-proxy", description = "Get or set the repository proxy")
public class RepositoryProxyCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "name", description = "The repository name", required = true, multiValued = false)
    @Completion(RepositoryNameCompleter.class)
    String name;

    @Argument(index = 1, name = "proxy", description = "The new repository proxy URL", required = false, multiValued = false)
    String proxy;

    @Option(name = "-m", aliases = { "--mirror" }, description = "Enable mirror mode")
    boolean mirror = false;

    @Override
    public Object execute() throws Exception {
        if (repositoryService.repository(name) == null) {
            System.err.println("Repository " + name + " doesn't exist");
            return null;
        }
        if (proxy != null) {
            repositoryService.changeProxy(name, proxy, mirror);
        }
        if (repositoryService.repository(name).isMirror()) {
            System.out.println(repositoryService.repository(name).getProxy() + " (mirror)");
        } else {
            System.out.println(repositoryService.repository(name).getProxy());
        }
        return null;
    }

}
