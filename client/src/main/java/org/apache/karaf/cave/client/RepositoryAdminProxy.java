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
package org.apache.karaf.cave.client;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Implementation of the OSGi RepositoryAdmin service which proxy to a Karaf Cave server.
 */
public class RepositoryAdminProxy implements RepositoryAdmin {

    private final String caveServerUrl;

    private final Map<String, RepositoryImpl> repositories = new HashMap<String, RepositoryImpl>();
    private final DataModelHelper dataModelHelper = new DataModelHelperImpl();

    public RepositoryAdminProxy(String caveServerUrl) {
        this.caveServerUrl = caveServerUrl;
    }

    public Resource[] discoverResources(String filter) throws InvalidSyntaxException {
        // check if the syntax is valid
        dataModelHelper.filter(filter);
        // delegate to the Cave server
        WebClient client = WebClient.create(caveServerUrl + "/obr/resources");

    }

    public Resource[] discoverResources(Requirement[] requirements) {
        WebClient client = WebClient.create(caveServerUrl + "/obr/resources");
    }


}
