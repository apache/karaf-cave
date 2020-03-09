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
package org.apache.karaf.cave.repository.service;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.scheduler.Scheduler;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.http.HttpService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static org.apache.karaf.cave.repository.service.RepositoryServiceImpl.convertMvnUrlToPath;
import static org.apache.karaf.cave.repository.service.RepositoryServiceImpl.parseMvnUrl;

public class RepositoryServiceImplTest {

    private RepositoryServiceImpl repositoryService;

    @Before
    public void setup() throws Exception {
        HttpService httpService = EasyMock.createMock(HttpService.class);
        Scheduler scheduler = EasyMock.createMock(Scheduler.class);
        repositoryService = new RepositoryServiceImpl();
        repositoryService.setHttpService(httpService);
        repositoryService.setScheduler(scheduler);
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("storage.location", "target/repositories");
        repositoryService.activate(properties);
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
    }

    @After
    public void teardown() throws Exception {
        // cleanup repositories
        if (Files.exists(Paths.get("target/repositories"))) {
            Files.walkFileTree(Paths.get("target/repositories"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        if (Files.exists(Paths.get("target/new-location"))) {
            Files.walkFileTree(Paths.get("target/new-location"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Test
    public void testStoreLoadRepositoryDb() throws Exception {
        repositoryService.create("first");
        repositoryService.create("second");
        Assert.assertEquals(2, repositoryService.repositories().size());
        repositoryService.save();
        repositoryService.clear();
        Assert.assertEquals(0, repositoryService.repositories().size());
        repositoryService.load();
        Assert.assertEquals(2, repositoryService.repositories().size());
        Repository first = repositoryService.repository("first");
        Assert.assertEquals("first", first.getName());
        Assert.assertEquals((new File("target/repositories/first").getAbsolutePath()), first.getLocation());
        Assert.assertNull(first.getProxy());
        Assert.assertEquals("karaf", first.getRealm());
        Assert.assertEquals(8, first.getPoolSize());
        Assert.assertEquals("/cave/repository/first", first.getUrl());
        Assert.assertNull(first.getUploadRole());
        Assert.assertNull(first.getDownloadRole());
        Repository second = repositoryService.repository("second");
        Assert.assertEquals("second", second.getName());
        Assert.assertEquals((new File("target/repositories/second").getAbsolutePath()), second.getLocation());
        Assert.assertNull(second.getProxy());
        Assert.assertEquals("karaf", second.getRealm());
        Assert.assertEquals(8, second.getPoolSize());
        Assert.assertEquals("/cave/repository/second", second.getUrl());
        Assert.assertNull(second.getUploadRole());
        Assert.assertNull(second.getDownloadRole());
    }

    @Test
    public void testCreateRepository() throws Exception {
        Repository repository = repositoryService.create("test");
        Assert.assertEquals("test", repository.getName());
        File file = new File("target/repositories/test");
        Assert.assertEquals(file.getAbsolutePath(), repository.getLocation());
        Assert.assertNull(repository.getProxy());
        Assert.assertEquals("karaf", repository.getRealm());
        Assert.assertNull(repository.getDownloadRole());
        Assert.assertNull(repository.getUploadRole());
        Assert.assertEquals(8, repository.getPoolSize());
        Assert.assertNotNull(repository);
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test")));
        Assert.assertTrue(Files.isDirectory(Paths.get("target/repositories/test")));
    }

    @Test
    public void testRemoveRepository() throws Exception {
        repositoryService.create("test");
        Assert.assertNotNull(repositoryService.repository("test"));
        Assert.assertEquals(1, repositoryService.repositories().size());
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test")));
        repositoryService.remove("test", true);
        Assert.assertNull(repositoryService.repository("test"));
        Assert.assertEquals(0, repositoryService.repositories().size());
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test")));
    }

    @Test
    public void testPurgeRepository() throws Exception {
        repositoryService.create("test");
        populateRepository("test");
        repositoryService.purge("test");
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test/testfile")));
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test/inner")));
    }

    @Test
    public void testCopyRepository() throws Exception {
        repositoryService.create("test");
        populateRepository("test");
        repositoryService.create("copy");
        repositoryService.copy("test", "copy");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/copy/testfile")));
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/copy/inner/folder/test/testfile")));
    }

    @Test
    public void testChangeRepositoryLocation() throws Exception {
        repositoryService.create("test");
        populateRepository("test");
        repositoryService.changeLocation("test", "target/new-location");
        Assert.assertTrue(Files.exists(Paths.get("target/new-location/testfile")));
        Assert.assertTrue(Files.exists(Paths.get("target/new-location/inner/folder/test/testfile")));
        Repository repository = repositoryService.repository("test");
        File file = new File("target/new-location");
        Assert.assertEquals(file.getAbsolutePath(), repository.getLocation());
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test")));
    }

    @Test
    public void testAddArtifactViaMvn() throws Exception {
        repositoryService.create("test");
        repositoryService.addArtifact("mvn:commons-lang/commons-lang/2.6", "test");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")));
    }

    @Test
    public void testAddArtifactViaHttp() throws Exception {
        repositoryService.create("test");
        repositoryService.addArtifact("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar", "test");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/commons-lang-2.6/commons-lang-2.6-.jar")));
    }

    @Test
    public void testDeleteArtifactByPath() throws Exception {
        repositoryService.create("test");
        populateRepository("test");
        repositoryService.deleteArtifact("inner/folder/test", "test");
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test/inner/folder/test")));
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/inner/folder")));
    }

    @Test
    public void testDeleteArtifactByMvnUrl() throws Exception {
        repositoryService.create("test");
        repositoryService.addArtifact("mvn:commons-lang/commons-lang/2.6", "test");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")));
        repositoryService.deleteArtifact("mvn:commons-lang/commons-lang/2.6", "test");
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")));
    }

    @Test
    public void testDeleteArtifactByMvnCoordinates() throws Exception {
        repositoryService.create("test");
        repositoryService.addArtifact("mvn:commons-lang/commons-lang/2.6", "test");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")));
        repositoryService.deleteArtifact("commons-lang", "commons-lang", "2.6", null, null, "test");
        Assert.assertFalse(Files.exists(Paths.get("target/repositories/test/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")));
    }

    @Test
    public void testUpdateBundleRepositoryDescriptor() throws Exception {
        repositoryService.create("test");
        repositoryService.addArtifact("mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.elasticsearch/7.3.2_1", "test");
        repositoryService.updateBundleRepositoryDescriptor("test");
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/test/repository.xml")));
    }

    @Test
    public void testMvnParser() throws Exception {
        Map<String, String> coordinates = parseMvnUrl("mvn:foo/bar/1.0");
        Assert.assertEquals("foo", coordinates.get("groupId"));
        Assert.assertEquals("bar", coordinates.get("artifactId"));
        Assert.assertEquals("1.0", coordinates.get("version"));
        Assert.assertEquals("jar", coordinates.get("extension"));

        coordinates = parseMvnUrl("mvn:otherg/othera/2.0/xml");
        Assert.assertEquals("otherg", coordinates.get("groupId"));
        Assert.assertEquals("othera", coordinates.get("artifactId"));
        Assert.assertEquals("2.0", coordinates.get("version"));
        Assert.assertEquals("xml", coordinates.get("extension"));

        coordinates = parseMvnUrl("mvn:thirdg/thirda/3.0/zip/myclassifier");
        Assert.assertEquals("thirdg", coordinates.get("groupId"));
        Assert.assertEquals("thirda", coordinates.get("artifactId"));
        Assert.assertEquals("3.0", coordinates.get("version"));
        Assert.assertEquals("zip", coordinates.get("extension"));
        Assert.assertEquals("myclassifier", coordinates.get("classifier"));
    }

    @Test
    public void testMvnUrl() throws Exception {
        Assert.assertEquals("group/foo/bar/1.0/bar-1.0.jar", convertMvnUrlToPath("mvn:group.foo/bar/1.0"));
        Assert.assertEquals("another/long/group/my.artifact/2.0/my.artifact-2.0.zip", convertMvnUrlToPath("mvn:another.long.group/my.artifact/2.0/zip"));
        Assert.assertEquals("my/group/my.artifact/3.0/my.artifact-3.0-myclassifier.xml", convertMvnUrlToPath("mvn:my.group/my.artifact/3.0/xml/myclassifier"));
    }

    private void populateRepository(String name) throws Exception {
        File testFile = new File("target/repositories/" + name + "/testfile");
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("This is a test");
        }
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/" + name + "/testfile")));
        Files.createDirectories(Paths.get("target/repositories/" + name + "/inner/folder/test"));
        File testInnerFile = new File("target/repositories/" + name + "/inner/folder/test/testfile");
        try (PrintWriter writer = new PrintWriter(new FileWriter(testInnerFile))) {
            writer.println("This is an inner file");
        }
        Assert.assertTrue(Files.exists(Paths.get("target/repositories/" + name + "/inner/folder/test/testfile")));
    }

}
