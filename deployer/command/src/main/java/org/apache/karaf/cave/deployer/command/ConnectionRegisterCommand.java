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

import org.apache.karaf.cave.deployer.api.Connection;
import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Service
@Command(scope = "cave", name = "deployer-connection-register", description = "Register a connection in the deployer service")
public class ConnectionRegisterCommand implements Action {

    @Reference
    private Deployer deployer;

    @Argument(index = 0, name = "name", description = "Name of the connection", required = true, multiValued = false)
    String name;

    @Argument(index = 1, name = "jmxUrl", description= "JMX URL of the Karaf instance", required = true, multiValued = false)
    String jmxUrl;

    @Argument(index = 2, name = "karafName", description = "Name of the Karaf instance", required = true, multiValued = false)
    String karafName;

    @Argument(index = 3, name = "username", description = "Username on the Karaf instance", required = true, multiValued = false)
    String username;

    @Argument(index = 4, name = "password", description = "Password on the Karaf instance", required = true, multiValued = false)
    String password;

    @Override
    public Object execute() throws Exception {
        Connection connection = new Connection();
        connection.setName(name);
        connection.setJmxUrl(jmxUrl);
        connection.setKarafName(karafName);
        connection.setUser(username);
        connection.setPassword(password);
        deployer.registerConnection(connection);
        return null;
    }

}
