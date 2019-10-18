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
package org.apache.karaf.cave.repository.service.scheduler;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;
import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryJob.class);

    private Repository repository;
    private RepositoryService repositoryService;

    public RepositoryJob(RepositoryService repositoryService, Repository repository) {
        this.repository = repository;
        this.repositoryService = repositoryService;
    }

    @Override
    public void execute(JobContext jobContext) {
        LOGGER.info("Executing scheduler for repository {}", repository.getName());
        String[] actions = repository.getSchedulingAction().split(",");
        for (String action : actions) {
            if (action.equalsIgnoreCase("purge")) {
                try {
                    repositoryService.purge(repository.getName());
                } catch (Exception e) {
                    LOGGER.error("Can't purge repository {}", repository.getName(), e);
                }
            } else if (action.equalsIgnoreCase("delete")) {
                try {
                    repositoryService.remove(repository.getName());
                } catch (Exception e) {
                    LOGGER.error("Can't delete repository {}", repository.getName(), e);
                }
            } else if (action.contains("copy")) {
                String[] destinationRepository = action.split(" ");
                if (destinationRepository.length != 2) {
                    LOGGER.error("Ambiguous destination repository in action {} for repository {}", action, repository.getName());
                } else {
                    try {
                        repositoryService.copy(repository.getName(), destinationRepository[1]);
                    } catch (Exception e) {
                        LOGGER.error("Can't copy repository {} to repository {}", repository.getName(), destinationRepository[1], e);
                    }
                }
            } else {
                LOGGER.error("Unknown scheduling action {} in repository {}", action, repository.getName());
            }
        }
    }

}