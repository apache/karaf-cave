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
