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

import org.apache.karaf.cave.server.api.CaveFeatureGateway;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CaveFeatureGatewayImpl implements CaveFeatureGateway {

    @Override
    public void register(String url) throws Exception {
        File storage = new File(STORAGE);

        Features features = new Features();
        if (storage.exists()) {
            features = JaxbUtil.unmarshal(storage.getAbsolutePath(), true);
        } else {
            storage.getParentFile().mkdirs();
            storage.createNewFile();
        }
        features.setName("cave-gateway");
        if (isFeaturesRepositoryRegistered(url, features.getRepository())) {
            throw new IllegalArgumentException("Features repository " + url + " already registered in the gateway");
        }
        features.getRepository().add(url);
        JaxbUtil.marshal(features, new FileOutputStream(storage));
    }

    @Override
    public void remove(String url) throws Exception {
        File storage = new File(STORAGE);

        if (!storage.exists()) {
            return;
        }

        Features features = JaxbUtil.unmarshal(storage.getAbsolutePath(), true);
        if (!isFeaturesRepositoryRegistered(url, features.getRepository())) {
            throw new IllegalArgumentException("Features repository " + url + " is not registered in the gateway");
        }
        features.getRepository().remove(url);
        JaxbUtil.marshal(features, new FileOutputStream(storage));
    }

    @Override
    public List<String> list() throws Exception {
        List<String> repositories = new ArrayList<>();

        File storage = new File(STORAGE);
        if (storage.exists()) {
            Features features = JaxbUtil.unmarshal(storage.getAbsolutePath(), true);
            repositories = features.getRepository();
        }

        return repositories;
    }

    private boolean isFeaturesRepositoryRegistered(String url, List<String> repositories) {
        for (String repository : repositories) {
            if (repository.equals(url)) {
                return true;
            }
        }
        return false;
    }
}
