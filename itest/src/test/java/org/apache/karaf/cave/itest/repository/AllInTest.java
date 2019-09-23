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
public class AllInTest extends KarafTestSupport {

    @Configuration
    public Option[] config() {
        Option[] options = new Option[]{
                KarafDistributionOption.editConfigurationFilePut("etc/system.properties", "cave.version", System.getProperty("cave.version"))
        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Test
    public void test() throws Exception {
        System.out.println(executeCommand("feature:repo-add cave " + System.getProperty("cave.version")));
        System.out.println("==== Install cave-repository, cave-deployer, cave-features-gateway ====");
        System.out.println(executeCommand("feature:install cave-repository", new RolePrincipal("admin")));
        System.out.println(executeCommand("feature:install cave-deployer", new RolePrincipal("admin")));
        System.out.println(executeCommand("feature:install cave-features-gateway", new RolePrincipal("admin")));

        Thread.sleep(1000);

        System.out.println("==== Installed Features ====");
        String featureList = executeCommand("feature:list -i");
        assertContains("cave-repository", featureList);
        assertContains("cave-deployer", featureList);
        assertContains("cave-features-gateway", featureList);
        System.out.println(featureList);
        String httpList = executeCommand("http:list");
        System.out.println("==== HTTP List ====");
        System.out.println(httpList);
        assertContains("/cave/features-gateway/api", httpList);
        assertContains("/cave/repository/api", httpList);
        assertContains("/cave/deployer/api", httpList);
        assertContainsNot("Failed", httpList);
    }

}
