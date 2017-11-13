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
    @Ignore
    public void installKarTest() throws Exception {
        System.out.println("Deploy a kar in a Karaf instance.");
        System.out.println("WARNING: this test requires a running Karaf instance");

        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setRepositoryUrl("file:target/test/repository");
        uploadRequest.setArtifactUrl("file:src/test/resources/test.kar");
        uploadRequest.setGroupId("kar-test");
        uploadRequest.setArtifactId("kar-test");
        uploadRequest.setVersion("1.0-SNAPSHOT");
        rest.uploadArtifact(uploadRequest);

        DeployRequest request = new DeployRequest();
        request.setArtifactUrl("mvn:kar-test/kar-test/1.0-SNAPSHOT/kar");
        request.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        request.setKarafName("root");
        request.setUser("karaf");
        request.setPassword("karaf");
        rest.installKar(request);
    }

    @Test
    public void explodeKarTest() throws Exception {
        System.out.println("This test is step 1 in the use case:");
        System.out.println("\t- User creates a kar locally");
        System.out.println("\t- The kar is uploaded on Maven repo (using mvn deploy:deploy-file or API");
        System.out.println("\t- The kar is exploded on the Maven repo");

        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setRepositoryUrl("file:target/test/repository");
        uploadRequest.setArtifactUrl("file:src/test/resources/test.kar");
        uploadRequest.setGroupId("kar-test");
        uploadRequest.setArtifactId("kar-test");
        uploadRequest.setVersion("1.0-SNAPSHOT");
        rest.uploadArtifact(uploadRequest);

        KarExplodeRequest karExplodeRequest = new KarExplodeRequest();
        // TODO: the artifact URL should be the one on Maven repository
        // To simplify we use test resources location directly
        karExplodeRequest.setArtifactUrl("file:src/test/resources/test.kar");
        karExplodeRequest.setRepositoryUrl("file:target/test/repository");
        rest.explodeKar(karExplodeRequest);
    }

    @Test
    @Ignore
    public void installFeatureTest() throws Exception {
        System.out.println("This test is the following step in the use case (if user doesn't want to create an uber feature)");
        System.out.println("WARNING: This test requires a running Karaf instance");
        System.out.println("- It registers a features repository");
        System.out.println("- Then installs a feature");

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("mvn:org.apache.camel.karaf/apache-camel/2.17.1/xml/features");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.addFeaturesRepository(deployRequest);

        deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("camel-blueprint");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.installFeature(deployRequest);
    }

    @Test
    @Ignore
    public void listingTest() throws Exception {
        System.out.println("WARNING: this test requires an active container.");

        BasicRequest request = new BasicRequest();
        request.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        request.setKarafName("root");
        request.setUser("karaf");
        request.setPassword("karaf");

        System.out.println(rest.listBundles(request));
        System.out.println(rest.listKars(request));
        System.out.println(rest.listFeaturesRepositories(request));
        System.out.println(rest.listFeatures(request));
    }

    @Test
    @Ignore
    public void assembleFeatureTest() throws Exception {
        System.out.println("This test is a second step in the use case.");
        System.out.println("The user creates a \"meta\" feature, assembling existing feature");
        System.out.println("Then he can install this \"meta\" feature");

        FeatureAssembleRequest assembleRequest = new FeatureAssembleRequest();
        assembleRequest.setVersion("1.0-SNAPSHOT");
        assembleRequest.setArtifactId("business-feature");
        assembleRequest.setFeature("business-feature");
        assembleRequest.setGroupId("business-feature");
        assembleRequest.setRepositoryUrl("file:" + System.getProperty("user.home") + "/.m2/repository");
        assembleRequest.setFeatureRepositories(Arrays.asList(new String[]{ "mvn:org.apache.camel.karaf/apache-camel/2.17.1/xml/features" }));
        assembleRequest.setFeatures(Arrays.asList(new String[]{ "camel-blueprint", "camel-stream" }));
        rest.assembleFeature(assembleRequest);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("mvn:business-feature/business-feature/1.0-SNAPSHOT/xml/features");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.addFeaturesRepository(deployRequest);

        deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("business-feature");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.installFeature(deployRequest);
    }

}
