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
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "repository-create", description = "Create a new repository")
public class RepositoryCreateCommand implements Action {

    @Reference
    private RepositoryService repositoryService;

    @Argument(index = 0, name = "name", description = "The repository name", required = true, multiValued = false)
    String name;

    @Option(name = "-l", aliases = { "--location" }, description = "The repository location", required = false, multiValued = false)
    String location;

    @Option(name = "-u", aliases = { "--url" }, description = "The repository URL", required = false, multiValued = false)
    String url;

    @Option(name = "-p", aliases = { "--proxy" }, description = "The repository proxy locations", required = false, multiValued = false)
    String proxy;

    @Option(name = "-m", aliases = { "--mirror" }, description = "Enable repository mirror mode (for proxy)", required = false, multiValued = false)
    boolean mirror = false;

    @Option(name = "-r", aliases = { "--realm" }, description = "The repository security realm", required = false, multiValued = false)
    String realm = "karaf";

    @Option(name = "-dr", aliases = { "--download-role"}, description = "The repository security download role", required = false, multiValued = false)
    String downloadRole;

    @Option(name = "-ur", aliases = { "--upload-role" }, description = "The repository security upload role", required = false, multiValued = false)
    String uploadRole;

    @Option(name = "-s", aliases = { "--scheduling", "--schedule" }, description = "The repository scheduling (cron: or at:)", required = false, multiValued = false)
    String scheduling;

    @Option(name = "-sa", aliases = { "--scheduling-action", "--action", "--actions" }, description = "The repository scheduling action (DELETE, PURGE, COPY)", required = false, multiValued = false)
    String schedulingAction;

    @Option(name = "-ps", aliases = { "--pool-size" }, description = "The repository pool size for the HTTP service", required = false, multiValued = false)
    int poolSize = 8;

    @Override
    public Object execute() throws Exception {
        repositoryService.create(name, location, url, proxy, mirror, realm, downloadRole, uploadRole, scheduling, schedulingAction, poolSize);
        return null;
    }

}
