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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.cave.server.api.CaveRepository;

/**
 * Create a Cave repository.
 */
@Command(scope = "cave", name = "repository-create", description = "Create a Cave repository")
public class RepositoryCreateCommand extends CaveRepositoryCommandSupport {

    @Option(name = "-l", aliases = {"--location"}, description = "Location of the repository on the file system", required = false, multiValued = false)
    String location;

    @Option(name = "-no", aliases = {"--no-obr-generate"}, description = "Do not generate OBR metadata", required = false, multiValued = false)
    boolean noOBRGenerate = false;

    @Option(name = "-ni", aliases = {"--no-install"}, description = "Do not install the repository in the OBR service", required = false, multiValued = false)
    boolean noInstall = false;

    @Argument(index = 0, name = "name", description = "The name of the repository", required = true, multiValued = false)
    String name = null;

    protected Object doExecute() throws Exception {
        if (getCaveRepositoryService().getRepository(name) != null) {
            System.err.println("Cave repository " + name + " already exists");
            return null;
        }
        if (location != null) {
            getCaveRepositoryService().create(name, location, false);
        } else {
            getCaveRepositoryService().create(name, false);
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(name);
        if (!noOBRGenerate) {
            caveRepository.scan();
        }
        if (!noInstall) {
            getCaveRepositoryService().install(name);
        }
        return null;
    }

}
