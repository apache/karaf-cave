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

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Create a Cave repository.
 */
@Command(scope = "cave", name = "repository-create", description = "Create a Cave repository")
@Service
public class RepositoryCreateCommand extends CaveRepositoryCommandSupport {

    @Option(name = "-l", aliases = {"--location"}, description = "Location of the repository on the file system", required = false, multiValued = false)
    String location;

    @Option(name = "-r", aliases = {"--realm"}, description = "JAAS realm to use for repository security", required = false, multiValued = false)
    String realm;

    @Option(name = "-d", aliases = {"--download-role"}, description = "Users role allowed to download artifacts", required = false, multiValued = false)
    String downloadRole;

    @Option(name = "-u", aliases = {"--upload-role"}, description = "Users role allowed to upload artifacts", required = false, multiValued = false)
    String uploadRole;

    @Option(name = "-no", aliases = {"--no-generate"}, description = "Do not generate repository metadata", required = false, multiValued = false)
    boolean noGenerate = false;

    @Option(name = "-ns", aliases = {"--no-start"}, description = "Do not start the Repository Service", required = false, multiValued = false)
    boolean noInstall = false;

    @Argument(index = 0, name = "name", description = "The name of the repository", required = true, multiValued = false)
    String name = null;

    protected Object doExecute() throws Exception {
        if (getCaveRepositoryService().getRepository(name) != null) {
            System.err.println("Cave repository " + name + " already exists");
            return null;
        }
        if (location != null) {
            getCaveRepositoryService().create(name, location, realm, downloadRole, uploadRole, false);
        } else {
            getCaveRepositoryService().create(name, false);
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(name);
        if (!noGenerate) {
            caveRepository.scan();
        }
        if (!noInstall) {
            getCaveRepositoryService().install(name);
        }
        return null;
    }

}
