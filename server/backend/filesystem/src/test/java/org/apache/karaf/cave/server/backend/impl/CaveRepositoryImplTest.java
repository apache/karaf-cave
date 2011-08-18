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
package org.apache.karaf.cave.server.backend.impl;

import org.apache.karaf.cave.server.backend.api.CaveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.URL;

/**
 * Unit test of the Cave Repository Implementation.
 */
@RunWith(JUnit4.class)
public class CaveRepositoryImplTest {

    private CaveRepositoryImpl repository;

    @Before
    public void setUp() throws Exception {
        repository = new CaveRepositoryImpl("test", "target/test-repository", false);
    }

    @Test
    public void testUploadBundleFromURL() throws Exception {
        repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-beanutils/1.8.2_2/org.apache.servicemix.bundles.commons-beanutils-1.8.2_2.jar"));
        repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-lang/2.4_5/org.apache.servicemix.bundles.commons-lang-2.4_5.jar"));
    }

    @Test
    public void testUploadNonBundleFromURL() throws Exception {
        repository.upload(new URL("http://repo1.maven.org/maven2/commons-vfs/commons-vfs/1.0/commons-vfs-1.0.jar"));
    }

    @Test
    public void testHttpProxy() throws Exception {
        repository.proxyHttp("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-lang/");
        repository.proxyHttp("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.abdera/0.4.0-incubating_5/org.apache.servicemix.bundles.abdera-0.4.0-incubating_5.jar");
        repository.proxyHttp("https://repository.apache.org/content/groups/snapshots-group/commons-beanutils/commons-beanutils/1.8.4-SNAPSHOT/commons-beanutils-1.8.4-20110805.033640-1.jar");
    }

}
