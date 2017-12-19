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
import org.apache.karaf.cave.deployer.command.completers.ConnectionCompleter;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "deployer-config-property-delete", description = "Delete a configuration property on a remote instance")
public class ConfigPropertyDeleteCommand implements Action {

    @Reference
    private Deployer deployer;

    @Argument(index = 0, name = "connection", description = "The connection to use", required = true, multiValued = false)
    @Completion(ConnectionCompleter.class)
    String connection;

    @Argument(index = 1, name = "pid", description = "The configuration PID", required = true, multiValued = false)
    String pid;

    @Argument(index = 2, name = "key", description = "The property key", required = true, multiValued = false)
    String key;

    @Override
    public Object execute() throws Exception {
        deployer.deleteConfigProperty(pid, key, connection);
        return null;
    }

}
