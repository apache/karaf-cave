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
package org.apache.karaf.cave.gateway.service;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.http.HttpService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

public class FeaturesGatewayServiceImplTest {

    private FeaturesGatewayServiceImpl featuresGatewayService;

    @Before
    public void setup() throws Exception {
        HttpService httpService = EasyMock.createMock(HttpService.class);
        featuresGatewayService = new FeaturesGatewayServiceImpl();
        featuresGatewayService.setHttpService(httpService);
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("storage.location", "target/cave/cave-features-gateway.xml");
        featuresGatewayService.activate(properties);
    }

    @Test
    public void testRegistrationUnregistration() throws Exception {
        featuresGatewayService.register("mvn:org.apache.karaf.features/standard/4.2.6/xml/features");
        Assert.assertEquals(1, featuresGatewayService.list().size());
        Assert.assertEquals("mvn:org.apache.karaf.features/standard/4.2.6/xml/features", featuresGatewayService.list().get(0));
        Assert.assertTrue(Files.exists(Paths.get("target/cave/cave-features-gateway.xml")));

        featuresGatewayService.remove("mvn:org.apache.karaf.features/standard/4.2.6/xml/features");
        Assert.assertEquals(0, featuresGatewayService.list().size());
    }

}
