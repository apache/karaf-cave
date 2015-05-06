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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Command to list all Cave repositories
 */
@Command(scope = "cave", name = "repositories", description = "List all Cave repositories")
@Service
public class RepositoriesListCommand extends CaveRepositoryCommandSupport {

    protected Object doExecute() throws Exception {
        CaveRepository[] repositories = getCaveRepositoryService().getRepositories();

        ShellTable table = new ShellTable();
        table.column("Name");
        table.column("Location");

        for (int i = 0; i < repositories.length; i++) {
            table.addRow().addContent(repositories[i].getName(), repositories[i].getLocation());
        }

        table.print(System.out);

        return null;
    }

}
