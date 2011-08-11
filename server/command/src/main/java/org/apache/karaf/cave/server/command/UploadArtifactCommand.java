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
import org.apache.karaf.cave.server.backend.api.CaveRepository;

import java.net.URL;

/**
 *  Command to upload an artifact into a Karaf Cave repository.
 */
@Command(scope = "cave", name = "upload-artifact", description = "Upload an artifact in a Karaf Cave repository")
public class UploadArtifactCommand extends CaveRepositoryCommandSupport {

    @Argument(index = 0, name = "repository", description = "The name of the Karaf Cave repository", required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "artifact", description = "The URL of the artifact to upload", required = true, multiValued = false)
    String url = null;

    public Object doExecute() throws Exception {
        CaveRepository caveRepository = getExistingRepository(name);
        caveRepository.upload(new URL(url));
        return null;
    }

}
