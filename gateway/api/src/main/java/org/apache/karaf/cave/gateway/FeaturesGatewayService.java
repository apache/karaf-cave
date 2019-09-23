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
package org.apache.karaf.cave.gateway;

import java.util.List;

/**
 * Manage features gateway.
 */
public interface FeaturesGatewayService {

    /**
     * Register a new features repository in the gateway.
     *
     * @param url the features repository XML URL.
     */
    void register(String url) throws Exception;

    /**
     * Remove a features repository from the gateway.
     *
     * @param id the features repository name or URL.
     */
    void remove(String id) throws Exception;

    /**
     * List the features repositories registered in the gateway.
     *
     * @return the features repositories registered in the gateway.
     */
    List<String> list() throws Exception;

}
