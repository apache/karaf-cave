package org.apache.karaf.cave.server.storage;

import java.io.OutputStreamWriter;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.karaf.features.internal.repository.StaxParser;
import org.apache.karaf.features.internal.repository.XmlRepository;
import org.osgi.resource.Resource;

public class OsgiRepository extends XmlRepository {

    StaxParser.XmlRepository repository;

    public OsgiRepository() {
        this(UUID.randomUUID().toString());
        repository = new StaxParser.XmlRepository();
        String url = getUrl();
        getLoaders().put(url, new XmlLoader(url, repository));
    }

    public OsgiRepository(String url) {
        super(url);
    }

    public void addResource(Resource resource) {
        repository.resources.add(resource);
        super.addResource(resource);
    }

    public String getName() {
        return repository.name;
    }

    public void setName(String name) {
        repository.name = name;
    }

    public long getIncrement() {
        return repository.increment;
    }

    public void setIncrement(long increment) {
        repository.increment = increment;
    }

    public void writeRepository(OutputStreamWriter writer) throws XMLStreamException {
        StaxParser.write(repository, writer);
    }
}
