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
package org.apache.karaf.cave.deployer.rest;

import org.apache.karaf.cave.deployer.api.Deployer;
import org.apache.karaf.cave.deployer.service.impl.DeployerImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class DeployerRestTest {

    private DeployerRest rest;

    @Before
    public void init() throws Exception {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.io.tmpdir", "target");
        Deployer deployer = new DeployerImpl();
        rest = new DeployerRest();
        rest.setDeployer(deployer);
    }

    @Test
    public void explodeKarTest() throws Exception {
        System.out.println("This test is step 1 in the use case:");
        System.out.println("\t- User creates a kar locally");
        System.out.println("\t- The kar is uploaded on Maven repo (using mvn deploy:deploy-file or API");
        System.out.println("\t- The kar is exploded on the Maven repo");

        DeployerRest.UploadRequest uploadRequest = new DeployerRest.UploadRequest();
        uploadRequest.setRepositoryUrl("file:target/test/repository");
        uploadRequest.setArtifactUrl("file:src/test/resources/test.kar");
        uploadRequest.setGroupId("kar-test");
        uploadRequest.setArtifactId("kar-test");
        uploadRequest.setVersion("1.0-SNAPSHOT");
        rest.upload(uploadRequest);

        DeployerRest.KarExplodeRequest karExplodeRequest = new DeployerRest.KarExplodeRequest();
        // TODO: the artifact URL should be the one on Maven repository
        // To simplify we use test resources location directly
        karExplodeRequest.setArtifactUrl("file:src/test/resources/test.kar");
        karExplodeRequest.setRepositoryUrl("file:target/test/repository");
        rest.explode(karExplodeRequest);
    }

}
