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

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.BundleException;

import static org.apache.karaf.cave.server.storage.Utils.deleteRecursive;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test of the Cave Repository Implementation.
 */
@RunWith(JUnit4.class)
public class CaveRepositoryImplTest {

    private CaveRepositoryImpl repository;

    @Before
    public void setUp() throws Exception {
        deleteRecursive(Paths.get("target/test-repository"));
        repository = new CaveRepositoryImpl("test", "target/test-repository", false);
    }

    @Test
    public void testUploadBundleFromURL() throws Exception {
        repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-beanutils/1.8.2_2/org.apache.servicemix.bundles.commons-beanutils-1.8.2_2.jar"));
        repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-lang/2.4_5/org.apache.servicemix.bundles.commons-lang-2.4_5.jar"));
    }

    @Test
    public void testUploadNonBundleFromURL() throws Exception {
        try {
            repository.upload(new URL("http://repo1.maven.org/maven2/commons-vfs/commons-vfs/1.0/commons-vfs-1.0.jar"));
            fail("An exception should be raised that the artifact is not a bundle.");
        } catch (BundleException e) {
            assertTrue("Wrong exception returned", e.getMessage().contains("Unsupported 'Bundle-ManifestVersion'"));
        }
    }

    @Test
    public void testAlreadyExistingBundle() throws Exception {
        try {
            repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-beanutils/1.8.2_1/org.apache.servicemix.bundles.commons-beanutils-1.8.2_1.jar"));
            repository.upload(new URL("http://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.commons-beanutils/1.8.2_1/org.apache.servicemix.bundles.commons-beanutils-1.8.2_1.jar"));
            fail("An exception should be raised that the artifact already exists in the Cave repository.");
        } catch (IllegalArgumentException expected) {
            assertTrue("Wrong exception returned.", expected.getMessage().contains("artifact is already present in the Cave repository"));
        }
    }

    @Test
    public void testPopulateWithFile() throws Exception {
        repository.populate(new URL("http://repo1.maven.org/maven2/commons-lang/commons-lang/1.0/commons-lang-1.0.jar"), false);
        File result = new File("target/test-repository/commons-lang-1.0.jar");
        assertTrue(result.exists());
    }

    @Test
    public void testPopulateWithDirectory() throws Exception {
        repository.populate(new URL("http://repo1.maven.org/maven2/commons-lang/commons-lang/1.0/"), false);
        File result = new File("target/test-repository/commons-lang-1.0.jar");
        assertTrue(result.exists());
    }

    @Test
    public void testPopulateWithDirectoryNoTrail() throws Exception {
        repository.populate(new URL("http://repo1.maven.org/maven2/commons-lang/commons-lang/1.0"), false);
        File result = new File("target/test-repository/commons-lang-1.0.jar");
        assertTrue(result.exists());
    }

}
