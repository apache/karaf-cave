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

import java.util.List;

@Service
@Command(scope = "cave", name = "deployer-assemble-feature", description = "Create/assembly a feature based on existing resources")
public class AssembleFeatureCommand implements Action {

    @Reference
    private Deployer deployer;

    @Option(name = "-g", aliases = "--groupId", description = "Maven groupId", required = true, multiValued = false)
    String groupId;

    @Option(name = "-a", aliases = "--artifactId", description = "Maven artifactId", required = true, multiValued = false)
    String artifactId;

    @Option(name = "-v", aliases = "--version", description = "Maven version", required = true, multiValued = false)
    String version;

    @Argument(index = 1, name = "repository", description = "The location of the repository where to upload the assembled feature", required = true, multiValued = false)
    String repository;

    @Argument(index = 0, name = "feature", description = "Name of the assembled feature", required = true, multiValued = false)
    String feature;

    @Option(name = "-r", aliases = "--repositories", description = "The list of features repositories to include in the assembled feature", required = false, multiValued = true)
    List<String> repositories;

    @Option(name = "-f", aliases = "--features", description = "The list of features to include in the assembled feature", required = false, multiValued = true)
    List<String> features;

    @Option(name = "-b", aliases = "--bundles", description = "The list of bundles to include in the assembled feature", required = false, multiValued = true)
    List<String> bundles;

    @Override
    public Object execute() throws Exception {
        deployer.assembleFeature(groupId, artifactId, version, repository, feature, repositories, features, bundles, null);
        return null;
    }

}
