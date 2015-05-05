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
 * Upload an artifact into a Cave repository
 */
@Command(scope = "cave", name = "repository-upload", description = "Upload an artifact in a Cave repository")
@Service
public class RepositoryUploadArtifactCommand extends CaveRepositoryCommandSupport {

    @Argument(index = 0, name = "repository", description = "The name of the repository", required = true, multiValued = false)
    @Completion(RepositoryCompleter.class)
    String name = null;

    @Argument(index = 1, name = "artifact", description = "The URL of the artifact to upload", required = true, multiValued = false)
    String url = null;

    @Option(name = "-no", aliases = { "--no-update", "--no-refresh", "--no-obr-register" }, description = "Do not refresh the OBR service", required = false, multiValued = true)
    boolean noUpdate = false;

    public Object doExecute() throws Exception {
        if (getCaveRepositoryService().getRepository(name) == null) {
            System.err.println("Cave repository " + name + " doesn't exist");
            return null;
        }
        CaveRepository caveRepository = getCaveRepositoryService().getRepository(name);
        caveRepository.upload(new URL(url));
        if (!noUpdate) {
            getCaveRepositoryService().install(name);
        }
        return null;
    }

}
