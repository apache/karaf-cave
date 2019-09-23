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
package org.apache.karaf.cave.gateway.service;

import org.apache.karaf.cave.gateway.FeaturesGatewayService;
import org.apache.karaf.cave.gateway.service.http.FeaturesGatewayServlet;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

@Component(name = "org.apache.karaf.cave.gateway", immediate = true, service = FeaturesGatewayService.class)
public class FeaturesGatewayServiceImpl implements FeaturesGatewayService {

    @Reference
    private HttpService httpService;

    private String storage;
    private String alias;

    @Activate
    public void activate(ComponentContext componentContext) throws Exception {
        activate(componentContext.getProperties());
    }

    public void activate(Dictionary<String, Object> properties) throws Exception {
        storage = (properties.get("storage.location") != null) ? properties.get("storage.location").toString() : System.getProperty("karaf.data") + File.separator + "cave" + File.separator + "features-gateway.xml";
        alias = (properties.get("http.alias") != null) ? properties.get("http.alias").toString() : "/cave/features-gateway-repository";
        FeaturesGatewayServlet featuresGatewayServlet = new FeaturesGatewayServlet(storage);
        httpService.registerServlet(alias, featuresGatewayServlet, null, null);
    }

    @Deactivate
    public void deactivate() {
        httpService.unregister(alias);
    }

    @Override
    public void register(String url) throws Exception {
        File store = new File(storage);

        Features features = new Features();
        if (store.exists()) {
            features = JaxbUtil.unmarshal(store.getAbsolutePath(), true);
        } else {
            store.getParentFile().mkdirs();
            store.createNewFile();
        }
        features.setName("cave-features-gateway");
        if (isFeaturesRepositoryRegistered(url, features.getRepository())) {
            throw new IllegalArgumentException("Features repository " + url + " already registered in the gateway");
        }
        features.getRepository().add(url);

        try (FileOutputStream fos = new FileOutputStream(store)) {
            JaxbUtil.marshal(features, fos);
        }
    }

    @Override
    public void remove(String url) throws Exception {
        File store = new File(storage);

        if (!store.exists()) {
            return;
        }

        Features features = JaxbUtil.unmarshal(store.getAbsolutePath(), true);
        if (!isFeaturesRepositoryRegistered(url, features.getRepository())) {
            throw new IllegalArgumentException("Features repository " + url + " is not registered in the gateway");
        }
        features.getRepository().remove(url);

        try (FileOutputStream fos = new FileOutputStream(store)) {
            JaxbUtil.marshal(features, fos);
        }
    }

    @Override
    public List<String> list() throws Exception {
        List<String> featuresRepositories = new ArrayList<>();

        File store = new File(storage);
        if (store.exists()) {
            Features features = JaxbUtil.unmarshal(store.getAbsolutePath(), true);
            featuresRepositories = features.getRepository();
        }

        return featuresRepositories;
    }

    private boolean isFeaturesRepositoryRegistered(String url, List<String> repositories) {
        for (String repository : repositories) {
            if (repository.equals(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Only visible for testing.
     */
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

}
