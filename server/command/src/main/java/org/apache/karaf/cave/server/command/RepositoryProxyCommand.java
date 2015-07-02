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
package org.apache.karaf.cave.server.command;

import java.net.URL;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.command.completers.RepositoryCompleter;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Add an URL to proxy in the Cave repository.
 */
@Command(scope = "cave", name = "repository-proxy", description = "Proxy a given URL in the Karaf Cave repository")
@Service
public class RepositoryProxyCommand extends CaveRepositoryCommandSupport {

    @Argument(index = 0, name = "name", description = "The name of the repository", required = true, multiValued = false)
    @Completion(RepositoryCompleter.class)
    String name = null;

    @Argument(index = 1, name = "URL", description = "The URL to proxy", required = true, multiValued = false)
    String url = null;

    @Option(name = "-no", aliases = { "--no-update", "--no-refresh", "--no-register" }, description = "No refresh of the repository service", required = false, multiValued = false)
    boolean noUpdate = false;

    @Option(name = "-f", aliases = { "--filter" }, description = "Regex filter on the artifacts URL", required = false, multiValued = false)
    String filter;

    protected Object doExecute() throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            System.err.println("Cave repository " + name + " doesn't exist");
            return null;
        }
        CaveRepository repository = getCaveRepositoryService().getRepository(name);
        repository.proxy(new URL(url), filter);
        if (!noUpdate) {
            getCaveRepositoryService().install(name);
        }
        return null;
    }

}
