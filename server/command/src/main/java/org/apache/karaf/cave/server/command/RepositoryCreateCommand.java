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
 * Command to create a Karaf Cave repository.
 */
@Command(scope = "cave", name = "repository-create", description = "Create a new Karaf Cave repository")
public class RepositoryCreateCommand extends CaveRepositoryCommandSupport {

    @Option(name = "-l", aliases = {"--location"}, description = "Location of the new repository on the file system", required = false, multiValued = false)
    String location;

    @Option(name = "-nu", aliases = {"--no-update"}, description = "Do not generate OBR metadata during creation", required = false, multiValued = false)
    boolean noUpdate = false;

    @Option(name = "-nr", aliases = {"--no-register"}, description = "Do not register the repository within the OBR service", required = false, multiValued = false)
    boolean noRegister = false;

    @Argument(index = 0, name = "name", description = "The name of the repository", required = true, multiValued = false)
    String name = null;

    protected Object doExecute() throws Exception {
        if (location != null) {
            getCaveRepositoryService().createRepository(name, location, false);
        } else {
            getCaveRepositoryService().createRepository(name, false);
        }
        CaveRepository caveRepository = getExistingRepository(name);
        if (!noUpdate) {
            caveRepository.scan();
        }
        if (!noRegister) {
            getCaveRepositoryService().install(name);
        }
        return null;
    }

}
