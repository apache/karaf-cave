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

import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;

/**
 * Abstract command describing the default behavior of Karaf Cave commands.
 */
public abstract class CaveRepositoryCommandSupport implements Action {

    @Reference
    private CaveRepositoryService caveRepositoryService;

    public CaveRepositoryService getCaveRepositoryService() {
        return this.caveRepositoryService;
    }

    @Override
    public Object execute() throws Exception {
        return doExecute();
    }

    protected abstract Object doExecute() throws Exception;

}
