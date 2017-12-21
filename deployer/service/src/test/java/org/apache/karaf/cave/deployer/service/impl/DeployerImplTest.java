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
package org.apache.karaf.cave.deployer.service.impl;

import org.apache.karaf.cave.deployer.api.Config;
import org.apache.karaf.cave.deployer.api.Deployer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeployerImplTest {

    private Deployer deployer;

    @Before
    public void startup() {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.io.tmpdir", "target");
        deployer = new DeployerImpl();
    }

    @Test
    public void downloadArtifactTest() throws Exception {
        deployer.download("mvn:commons-lang/commons-lang/2.6", "target/test/commons-lang-2.6.jar");
    }

    @Test
    public void extractTest() throws Exception {
        deployer.extract("mvn:org.apache.karaf/apache-karaf-minimal/4.1.3/zip", "target/test/karaf");
    }

    @Test
    public void explodeKarTest() throws Exception {
        deployer.explode("mvn:org.apache.karaf.features/framework/4.1.3/kar", "file:target/test/repository/kar");
    }

    @Test
    public void uploadArtifactTest() throws Exception {
        deployer.upload("test", "test", "1.0-SNAPSHOT", "mvn:commons-lang/commons-lang/2.6", "file:target/test/repository");
    }

    @Test
    public void assembleFeatureTest() throws Exception {
        List<String> featureRepositories = new ArrayList<String>();
        featureRepositories.add("mvn:org.apache.camel.karaf/apache-camel/2.17.2/xml/features");
        List<String> features = new ArrayList<String>();
        features.add("camel-spring");
        features.add("camel-jms");
        features.add("camel-stream");
        List<String> bundles = new ArrayList<String>();
        bundles.add("mvn:commons-lang/commons-lang/2.6");
        deployer.assembleFeature("test-feature", "test-feature", "1.0-SNAPSHOT", "file:target/test/repository", "test-feature", featureRepositories, features, bundles, null);
    }

    @Test
    public void assembleFeatureWithConfigTest() throws Exception {
        List<String> features = new ArrayList<String>();
        features.add("eventadmin");
        List<Config> configs = new ArrayList<Config>();
        Config config = new Config();
        config.setPid("org.mytest");
        config.getProperties().put("foo", "bar");
        config.getProperties().put("other", "value");
        configs.add(config);
        deployer.assembleFeature("config-feature", "config-feature", "1.0-SNAPSHOT", "file:target/test/repository", "config-feature", null, features, null, configs);
    }

    @Test
    public void assembleFeatureWithNullTest() throws Exception {
        deployer.assembleFeature("null-feature", "null-feature", "1.0-SNAPSHOT", "file:target/test/repository", "null-feature", null, null, null, null);
    }

    @Test
    public void mvnParseTest() throws Exception {
        String mvnUrl = "mvn:testGroupId/testArtifactId/1.0";
        Map<String, String> coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("jar", coordonates.get("extension"));
        Assert.assertNull(coordonates.get("classifier"));

        mvnUrl = "mvn:testGroupId/testArtifactId/1.0/kar";
        coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("kar", coordonates.get("extension"));
        Assert.assertNull(coordonates.get("classifier"));

        mvnUrl = "mvn:testGroupId/testArtifactId/1.0/xml/features";
        coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("xml", coordonates.get("extension"));
        Assert.assertEquals("features", coordonates.get("classifier"));
    }

}
