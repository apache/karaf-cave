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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.util.stream.Stream;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DeployerTest extends KarafTestSupport {

    @Configuration
    public Option[] config() {
        Option[] options = new Option[]{
                KarafDistributionOption.editConfigurationFilePut("etc/system.properties", "cave.version", System.getProperty("cave.version"))
        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Test(timeout = 60000L)
    public void test() throws Exception {
        System.out.println(executeCommand("feature:repo-add cave " + System.getProperty("cave.version")));
        System.out.println(executeCommand("feature:install cave-deployer", new RolePrincipal("admin")));
        String featureList = executeCommand("feature:list -i");
        while (!featureList.contains("cave-deployer")) {
            featureList = executeCommand("feature:list -i");
            Thread.sleep(100);
        }
        System.out.println("==== Installed Features ====");
        System.out.println(featureList);
        String httpList = executeCommand("http:list");
        System.out.println("==== HTTP List ====");
        System.out.println(httpList);
        assertContains("/cave/deployer/api", httpList);

        System.out.println("==== Create Connection ====");
        executeCommand("cave:deployer-connection-register TEST " + getJmxServiceUrl() + " root karaf karaf");
        System.out.println("==== Connections List ====");
        String connectionList = executeCommand("cave:deployer-connection-list");
        assertContains("TEST", connectionList);
        System.out.println(connectionList);

        System.out.println("==== List Features Via Deployer ====");
        String featureListViaDeployer = executeCommand("cave:deployer-feature-list TEST");
        assertContains("cave-deployer", featureListViaDeployer);
        System.out.println(featureListViaDeployer);
    }

}
