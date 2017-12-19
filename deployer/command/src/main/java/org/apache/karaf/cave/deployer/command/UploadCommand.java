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
package org.apache.karaf.cave.deployer.command;

import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "deployer-upload", description = "Upload an artifact to a Maven repository using the given coordinates")
public class UploadCommand implements Action {

    @Reference
    private Deployer deployer;

    @Option(name = "-g", aliases = "--groupId", description = "Maven groupId", required = true, multiValued = false)
    String groupId;

    @Option(name = "-a", aliases = "--artifactId", description = "Maven artifactId", required = true, multiValued = false)
    String artifactId;

    @Option(name = "-v", aliases = "--version", description = "Maven version", required = true, multiValued = false)
    String version;

    @Argument(index = 0, name = "artifact", description = "Location of the artifact", required = true, multiValued = false)
    String artifact;

    @Argument(index = 1, name = "repository", description = "Location of the Maven repository", required = true, multiValued = false)
    String repository;

    @Override
    public Object execute() throws Exception {
        deployer.upload(groupId, artifactId, version, artifact, repository);
        return null;
    }

}
