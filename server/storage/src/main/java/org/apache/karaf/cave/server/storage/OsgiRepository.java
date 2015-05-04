package org.apache.karaf.cave.server.storage;

import java.io.OutputStreamWriter;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.karaf.features.internal.repository.StaxParser;
import org.apache.karaf.features.internal.repository.XmlRepository;
import org.osgi.resource.Resource;

public class OsgiRepository extends XmlRepository {

    StaxParser.XmlRepository repository;

    public OsgiRepository(String url, String name) {
        this(url);
        repository = new StaxParser.XmlRepository();
        repository.name = name;
        getLoaders().put(url, new XmlLoader(url, repository));
    }

    public OsgiRepository(String url) {
        super(url);
    }

    public void addResource(Resource resource) {
        load();
        repository.resources.add(resource);
        super.addResource(resource);
    }

    public long getIncrement() {
        load();
        return repository.increment;
    }

    public void setIncrement(long increment) {
        load();
        repository.increment = increment;
    }

    public void writeRepository(OutputStreamWriter writer) throws XMLStreamException {
        StaxParser.write(repository, writer);
    }

    private void load() {
        // Force repository load
        getResources();
    }

}
