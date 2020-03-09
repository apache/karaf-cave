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
package org.apache.karaf.cave.itest.repository;

import org.apache.karaf.itests.KarafTestSupport;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RepositoryTest extends KarafTestSupport {

    @Configuration
    public Option[] config() {
        Option[] options = new Option[]{
                KarafDistributionOption.editConfigurationFilePut("etc/system.properties", "cave.version", System.getProperty("cave.version"))
        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Test
    public void installationTest() throws Exception {
        System.out.println(executeCommand("feature:repo-add cave " + System.getProperty("cave.version")));
        String repoList = executeCommand("feature:repo-list");
        System.out.println("==== Features Repositories ====");
        System.out.println(repoList);
        assertContains("mvn:org.apache.karaf.cave/apache-karaf-cave/" + System.getProperty("cave.version") + "/xml/features", repoList);
        System.out.println(executeCommand("feature:install cave-repository", new RolePrincipal("admin")));
        String featureList = executeCommand("feature:list -i");
        int count = 0;
        while (!featureList.contains("cave-repository") && count < 100) {
            featureList = executeCommand("feature:list -i");
            Thread.sleep(100);
            count++;
        }
        if (count >= 100) {
            throw new RuntimeException("cave-repository feature is not installed");
        }
        System.out.println("==== Installed Features ====");
        System.out.println(featureList);
        String httpList = executeCommand("http:list");
        System.out.println("==== HTTP List ====");
        System.out.println(httpList);
        assertContains("/cave/repository/api", httpList);
        String repositoryService = executeCommand("service:list org.apache.karaf.cave.repository.RepositoryService");
        assertContains("[org.apache.karaf.cave.repository.RepositoryService]", repositoryService);
        assertContains("component.name = org.apache.karaf.cave.repository", repositoryService);
        System.out.println("==== Cave Repository Service ====");
        System.out.println(repositoryService);
    }

    @Test(timeout = 60000L)
    public void repositoryTestViaCommands() throws Exception {
        installCaveRepository();
        System.out.println("==== Create TEST repository ====");
        executeCommand("cave:repository-create TEST");
        String repositoryList = executeCommand("cave:repository-list");
        assertContains("TEST", repositoryList);
        System.out.println("==== Repository List ====");
        System.out.println(repositoryList);
        System.out.println("==== HTTP List ====");
        String httpList = executeCommand("http:list");
        assertContains("TEST", httpList);
        System.out.println(httpList);

        System.out.println("==== Add Artifact in TEST ====");
        executeCommand("cave:repository-artifact-add TEST mvn:commons-lang/commons-lang/2.6");

        System.out.println("==== Check Artifact on TEST ====");
        URL testUrl = new URL("http://localhost:" + getHttpPort() + "/cave/repository/TEST/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
        Assert.assertEquals(200, connection.getResponseCode());
        Assert.assertEquals("OK", connection.getResponseMessage());
        Assert.assertEquals("application/octet-stream", connection.getContentType());

        System.out.println("==== Delete Artifact from TEST ====");
        executeCommand("cave:repository-artifact-delete TEST mvn:commons-lang/commons-lang/2.6");

        System.out.println("==== Delete TEST repository ====");
        executeCommand("cave:repository-remove TEST");
        repositoryList = executeCommand("cave:repository-list");
        assertContainsNot("TEST", repositoryList);

        System.out.println("==== Create PROXY repository ====");
        executeCommand("cave:repository-create -p https://repo1.maven.org/maven2\\@id=Central PROXY");

        System.out.println("==== Repository List ====");
        repositoryList = executeCommand("cave:repository-list");
        assertContains("PROXY", repositoryList);
        System.out.println(repositoryList);

        System.out.println("==== Repository Info PROXY ====");
        String repositoryInfo = executeCommand("cave:repository-info PROXY");
        assertContains("Central", repositoryInfo);
        System.out.println(repositoryInfo);

        System.out.println("==== Try to get artifact on PROXY ====");
        URL proxyUrl = new URL("http://localhost:" + getHttpPort() + "/cave/repository/PROXY/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        HttpURLConnection proxyUrlConnection = (HttpURLConnection) proxyUrl.openConnection();
        Assert.assertEquals(200, proxyUrlConnection.getResponseCode());
        Assert.assertEquals("OK", proxyUrlConnection.getResponseMessage());
        Assert.assertEquals("application/octet-stream", proxyUrlConnection.getContentType());
        Assert.assertEquals(284220, proxyUrlConnection.getContentLength());
        System.out.println(executeCommand("cave:repository-remove PROXY"));

        System.out.println("==== Create SCHEDULED repository ====");
        executeCommand("cave:repository-create -s \"cron:0/5 * * * * ?\" -sa DELETE SCHEDULED");
        repositoryInfo = executeCommand("cave:repository-info SCHEDULED");
        assertContains("DELETE", repositoryInfo);
        System.out.println(repositoryInfo);
        System.out.println("==== Scheduler Jobs ====");
        String scheduler = executeCommand("scheduler:list");
        assertContains("cave-repository-SCHEDULED", scheduler);
        System.out.println(scheduler);
        System.out.println("==== Wait Scheduler ====");
        repositoryList = executeCommand("cave:repository-list");
        while (repositoryList.contains("SCHEDULED")) {
            Thread.sleep(2000);
            repositoryList = executeCommand("cave:repository-list");
        }
        System.out.println(repositoryList);
    }

    private void installCaveRepository() throws Exception {
        executeCommand("feature:repo-add cave " + System.getProperty("cave.version"));
        executeCommand("feature:install cave-repository", new RolePrincipal("admin"));
    }

}
