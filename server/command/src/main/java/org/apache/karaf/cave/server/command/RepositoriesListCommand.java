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

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.cave.server.api.CaveRepository;

/**
 * Command to list all Karaf Cave repositories available.
 */
@Command(scope = "cave", name = "repositories-list", description = "List all Karaf Cave repositories")
public class RepositoriesListCommand extends CaveRepositoryCommandSupport {

    private static final String OUTPUT_FORMAT = "%-20s %-20s";

    protected Object doExecute() throws Exception {
        CaveRepository[] repositories = getCaveRepositoryService().getRepositories();

        System.out.println(String.format(OUTPUT_FORMAT, "Name", "Location"));
        for (int i = 0; i < repositories.length; i++) {
            System.out.println(String.format(OUTPUT_FORMAT, "[" + repositories[i].getName() + "]", "[" + repositories[i].getLocation() + "]"));
        }

        return null;
    }

}
