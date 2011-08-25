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
import org.apache.karaf.cave.server.backend.api.CaveRepository;

import java.net.URL;

/**
 * Command to populate a Karaf Cave repository from a given URL.
 */
@Command(scope = "cave", name = "populate-repository", description = "Populate a Karaf Cave repository with the artifacts present at the given URL")
public class PopulateRepositoryCommand extends CaveRepositoryCommandSupport {

    @Option(name = "-u", aliases = { "--update" }, description = "Update the OBR metadata on the fly", required = false, multiValued = false)
    boolean update = false;

    @Argument(index = 0, name = "name", description = "The name of the Karaf Cave repository", required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "url", description = "The source URL to scan", required = true, multiValued = false)
    String url = null;

    protected Object doExecute() throws Exception {
        CaveRepository repository = getExistingRepository(name);
        repository.populate(new URL(url), update);
        return null;
    }

}
