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
package org.apache.karaf.cave.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class CaveRestServlet extends CXFNonSpringServlet {

    private RepositoryRest repositoryRest;
    private DeployerRest deployerRest;

    public CaveRestServlet(RepositoryRest repositoryRest, DeployerRest deployerRest) {
        this.repositoryRest = repositoryRest;
        this.deployerRest = deployerRest;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        if (repositoryRest != null) {
            JAXRSServerFactoryBean repositoryBean = new JAXRSServerFactoryBean();
            repositoryBean.setAddress("/repository");
            repositoryBean.setBus(getBus());
            repositoryBean.setProvider(new JacksonJsonProvider());
            repositoryBean.setServiceBean(repositoryRest);
            repositoryBean.create();
        }

        if (deployerRest != null) {
            JAXRSServerFactoryBean deployerBean = new JAXRSServerFactoryBean();
            deployerBean.setAddress("/deployer");
            deployerBean.setBus(getBus());
            deployerBean.setProvider(new JacksonJsonProvider());
            deployerBean.setServiceBean(deployerRest);
            deployerBean.create();
        }

    }

}
