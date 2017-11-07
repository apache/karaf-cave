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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@XmlRootElement(name = "cave-repository")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Repository", propOrder = {"name", "increment", "location", "resources"})
public class Repository {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "increment")
    private long increment;
    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "resources")
    private List<Resource> resources;
    
    public Repository() {
        // require for JABX IllegalAnnotationExceptions
    }

    public Repository(CaveRepository repository) {
        this.name = repository.getName();
        this.increment = repository.getIncrement();
        this.location = repository.getLocation();

        try {
            Document doc = XmlUtils.parse(repository.getRepositoryXml().toExternalForm());

            // get all resource elements
            NodeList resourceList = doc.getElementsByTagName("resource");

            if (resourceList.getLength() > 0) {
                this.resources = new ArrayList<>();
            }

            // read resource
            for (int vI = 0; vI < resourceList.getLength(); vI++) {

                Element nodeResource = (Element) resourceList.item(vI);
                Resource resource = new Resource();

                NodeList capabilityList = nodeResource.getElementsByTagName("capability");

                // read capability
                for (int vInt = 0; vInt < capabilityList.getLength(); vInt++) {

                    Element capability = (Element) capabilityList.item(vInt);

                    if (capability.hasAttribute("namespace")) {

                        Element attribute = (Element) capability.getFirstChild();

                        while (attribute != null) {

                            if (capability.getAttribute("namespace").equals("osgi.identity")) {

                                switch (attribute.getAttribute("name")) {
                                    case "osgi.identity":
                                        resource.setIdentity(attribute.getAttribute("value"));
                                        break;
                                    case "type":
                                        resource.setType(attribute.getAttribute("value"));
                                        break;
                                    case "version":
                                        resource.setVersion(attribute.getAttribute("value"));
                                        break;
                                    default:
                                        break;
                                }

                            } else if (capability.getAttribute("namespace")
                                    .equals("osgi.content")) {
                                switch (attribute.getAttribute("name")) {
                                    case "url":
                                        // name of the artefact, the baseURI of the HttpServlet
                                        // for download is not available
                                        resource.setArtefact(attribute.getAttribute("value"));
                                        break;
                                    default:
                                        break;
                                }
                            }

                            attribute = (Element) attribute.getNextSibling();
                        }
                    }
                }

                getResources().add(resource);
            }

        } catch (Exception exception) {
            // do nothing
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public long getIncrement() {
        return increment;
    }

    public void setIncrement(long incrementIn) {
        increment = incrementIn;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resourcesIn) {
        resources = resourcesIn;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String locationIn) {
        location = locationIn;
    }

}
