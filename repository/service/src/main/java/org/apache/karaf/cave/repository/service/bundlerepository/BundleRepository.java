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
package org.apache.karaf.cave.repository.service.bundlerepository;

import org.osgi.resource.Resource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BundleRepository extends XmlRepository {

    OsgiLoader loader;

    public BundleRepository(String url, String name) {
        this(url);
        StaxParser.XmlRepository repository = new StaxParser.XmlRepository();
        repository.name = name;
        loader = new OsgiLoader(url, repository);
        getLoaders().put(url, loader);
    }

    public BundleRepository(String url) {
        super(url, -1, false);
    }

    public long getIncrement() {
        load();
        return loader.getXml().increment;
    }

    public void writeRepository(Writer writer) throws XMLStreamException {
        StaxParser.write(loader.getXml(), writer);
    }

    private void load() {
        // force repository load
        getResources();
    }

    public void addResourcesAndSave(List<Resource> resources) throws XMLStreamException, IOException {
        lock.writeLock().lock();
        try {
            load();
            for (Resource resource : resources) {
                loader.getXml().resources.add(resource);
                addResource(resource);
            }
            loader.getXml().increment = System.currentTimeMillis();
            try (Writer writer = Files.newBufferedWriter(Paths.get(URI.create(getUrl())), StandardCharsets.UTF_8)) {
                StaxParser.write(loader.getXml(), writer);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected static class OsgiLoader extends XmlLoader {

        public OsgiLoader(String url) {
            super(url, -1);
        }

        public OsgiLoader(String url, StaxParser.XmlRepository xml) {
            super(url, -1, xml);
        }

        public StaxParser.XmlRepository getXml() {
            return xml;
        }
    }

}
