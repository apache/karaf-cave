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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Command to install a Cave repository into the OBR service.
 */
@Command(scope = "cave", name = "repository-install", description = "Install a Cave repository in the OBR service")
public class RepositoryInstallCommand extends CaveRepositoryCommandSupport {

    @Argument(index = 0, name = "name", description = "The name of the repository", required = true, multiValued = false)
    String name = null;

    protected Object doExecute() throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            System.err.println("Cave repository " + name + " doesn't exist");
            return null;
        }
        getCaveRepositoryService().install(name);
        return null;
    }

}
