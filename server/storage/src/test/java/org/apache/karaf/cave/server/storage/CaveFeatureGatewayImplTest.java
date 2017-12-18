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
package org.apache.karaf.cave.server.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CaveFeatureGatewayImplTest {

    private CaveFeatureGatewayImpl gateway;

    @Before
    public void setup() throws Exception {
        System.setProperty("karaf.data", "target/test-classes");
        File file = new File("target/test-classes/cave-features-gateway.xml");
        if (file.exists()) {
            file.delete();
        }
        gateway = new CaveFeatureGatewayImpl();
    }

    @Test
    public void testRegister() throws Exception {
        gateway.register("mvn:test/test/1.0-SNAPSHOT");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"cave-gateway\">" +
                "    <repository>mvn:test/test/1.0-SNAPSHOT</repository>" +
                "</features>";
        String result = readCaveFile();
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRemove() throws Exception {
        gateway.register("mvn:test/second/1.0-SNAPSHOT");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"cave-gateway\">" +
                "    <repository>mvn:test/second/1.0-SNAPSHOT</repository>" +
                "</features>";
        String result = readCaveFile();
        Assert.assertEquals(expected, result);
        gateway.remove("mvn:test/second/1.0-SNAPSHOT");
        expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"cave-gateway\"/>";
        result = readCaveFile();
        Assert.assertEquals(expected, result);
    }

    private String readCaveFile() throws Exception {
        File file = new File("target/test-classes/cave-features-gateway.xml");
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}
