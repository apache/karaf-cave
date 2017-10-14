package org.apache.karaf.cave.server.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Resource", propOrder = {"identity", "type", "version", "artefact"})
public class Resource {

    @XmlElement(name = "identity")
    private String identity;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "version")
    private String version;
    @XmlElement(name = "artefact")
    private String artefact;


    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String pIdentity) {
        identity = pIdentity;
    }

    public String getType() {
        return type;
    }

    public void setType(String pType) {
        type = pType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    public String getArtefact() {
        return artefact;
    }

    public void setArtefact(String pArtefact) {
        artefact = pArtefact;
    }
}
