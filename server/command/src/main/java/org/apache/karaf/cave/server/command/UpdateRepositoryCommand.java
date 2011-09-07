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
import org.apache.karaf.cave.server.api.CaveRepository;

/**
 *  Command to update the OBR metadata
 */
@Command(scope = "cave", name = "update-repository", description = "Update OBR metadata of a Cave Repository")
public class UpdateRepositoryCommand extends CaveRepositoryCommandSupport {

    @Argument(index = 0, name = "name", description = "The name of the Karaf Cave repository", required = true, multiValued = false)
    String name = null;

    protected Object doExecute() throws Exception {
        CaveRepository caveRepository = getExistingRepository(name);
        caveRepository.scan();
        return null;
    }

}
