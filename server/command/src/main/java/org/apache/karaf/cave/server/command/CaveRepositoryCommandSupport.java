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
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * Abstract command describing the default behavior of Karaf Cave commands.
 */
public abstract class CaveRepositoryCommandSupport extends OsgiCommandSupport {

    private CaveRepositoryService caveRepositoryService;

    public CaveRepositoryService getCaveRepositoryService() {
        return this.caveRepositoryService;
    }

    public void setCaveRepositoryService(CaveRepositoryService caveRepositoryService) {
        this.caveRepositoryService = caveRepositoryService;
    }

    protected CaveRepository getExistingRepository(String name) {
        CaveRepository caveRepository = caveRepositoryService.getRepository(name);
        if (caveRepository == null) {
            throw new IllegalArgumentException("Cave repository " + name + " does not exist");
        }
        return caveRepository;
    }

}
