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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class FeaturesGatewayTest extends KarafTestSupport {

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
        System.out.println(executeCommand("feature:install cave-features-gateway", new RolePrincipal("admin")));
        String featureList = executeCommand("feature:list -i");
        int count = 0;
        while (!featureList.contains("cave-features-gateway") && count < 100) {
            featureList = executeCommand("feature:list -i");
            Thread.sleep(100);
            count++;
        }
        if (count >= 100) {
            throw new RuntimeException("cave-features-gateway feature is not installed");
        }
        System.out.println("==== Installed Features ====");
        System.out.println(featureList);
        String httpList = executeCommand("http:list");
        System.out.println("==== HTTP List ====");
        System.out.println(httpList);
        assertContains("/cave/features-gateway/api", httpList);

        // add camel features in the gateway
        System.out.println(executeCommand("cave:features-gateway-register mvn:org.apache.camel.karaf/apache-camel/2.24.2/xml/features"));
        String gatewayList = executeCommand("cave:features-gateway-list");
        System.out.println(gatewayList);
        assertContains("mvn:org.apache.camel.karaf/apache-camel/2.24.2/xml/features", gatewayList);

        // get the gateway features XML
        URL url = new URL("http://localhost:" + getHttpPort() + "/cave/features-gateway-repository");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("GET");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
        System.out.println(builder.toString());
        assertContains("mvn:org.apache.camel.karaf/apache-camel/2.24.2/xml/features", builder.toString());
    }

}
