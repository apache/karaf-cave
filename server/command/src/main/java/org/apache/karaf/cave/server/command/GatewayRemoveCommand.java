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

import org.apache.karaf.cave.server.api.CaveFeatureGateway;
import org.apache.karaf.cave.server.command.completers.GatewayCompleter;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "gateway-remove", description = "Remove a features repository from the gateway")
public class GatewayRemoveCommand implements Action {

    @Argument(name = "repository", description = "The features repository URL", required = true, multiValued = false)
    @Completion(GatewayCompleter.class)
    String repository;

    @Reference
    private CaveFeatureGateway gateway;

    @Override
    public Object execute() throws Exception {
        gateway.remove(repository);
        return null;
    }

}
