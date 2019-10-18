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
package org.apache.karaf.cave.repository;

/**
 * Cave repository.
 */
public class Repository {

    private String name;
    private String location;
    private String url;
    private String proxy;
    private boolean mirror;
    private String realm;
    private String downloadRole;
    private String uploadRole;
    private String scheduling;
    private String schedulingAction;
    private int poolSize;

    /**
     * Get repository name.
     *
     * @return the repository name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the repository name.
     *
     * @param name the repository name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the repository storage location.
     *
     * @return the repository storage location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set the repository storage location.
     *
     * @param location the repository storage location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get the HTTP URL of the repository.
     *
     * @return The repository HTTP URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the HTTP URL of the repository.
     *
     * @param url The repository HTTP URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the proxied repositories by this one.
     *
     * @return The repositories proxied by this repository.
     */
    public String getProxy() {
        return proxy;
    }

    /**
     * Set the proxied repositories by this one.
     *
     * @param proxy The repositories proxied by this repository.
     */
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    /**
     * Get the proxy mode (mirroring or not).
     *
     * @return The repositories proxy mode, true for mirroring, false else.
     */
    public boolean isMirror() {
        return mirror;
    }

    /**
     * Set the proxy mode (mirroring or not).
     *
     * @param mirror true to set the proxy mode to mirror, false else.
     */
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    /**
     * Get the JAAS realm used to secure the repository access.
     *
     * @return the name of JAAS realm.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Set the JAAS realm name used to secure the repository access.
     *
     * @param realm the name of the JAAS realm to use with this repository.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Get the user role allowed to download artifacts on this repository.
     *
     * @return the user role name.
     */
    public String getDownloadRole() {
        return downloadRole;
    }

    /**
     * Set the user role name allowed to download artifacts on this repository.
     *
     * @param downloadRole the user role name.
     */
    public void setDownloadRole(String downloadRole) {
        this.downloadRole = downloadRole;
    }

    /**
     * Get the user role allowed to upload artifacts on this repository.
     *
     * @return the user role name.
     */
    public String getUploadRole() {
        return uploadRole;
    }

    /**
     * Set the user role name allowed to upload artifacts on this repository.
     *
     * @param uploadRole the user role name.
     */
    public void setUploadRole(String uploadRole) {
        this.uploadRole = uploadRole;
    }

    /**
     * Get the scheduling of the repository. Valid format is cron:xx, at:xx or simply cron.
     * For instance:
     *    cron:0 0/10 * * * ?
     *    at:2014-05-13T13:56:45
     * @return the current repository scheduling or {@code null} if not defined.
     */
    public String getScheduling() {
        return scheduling;
    }

    /**
     * Set the repository scheduling. Valid format is cron:xx, at:xx or simply cron.
     * For instance:
     *    cron:0 0/10 * * * ?
     *    at:2014-05-13T13:56:45
     *
     * @param scheduling the new repository scheduling.
     */
    public void setScheduling(String scheduling) {
        this.scheduling = scheduling;
    }

    /**
     * Get the action performed at scheduling. Valid values are DELETE, PURGE, COPY (it can be combined using comma as separator).
     *
     * @return the action performed at scheduling (DELETE, PURGE, COPY).
     */
    public String getSchedulingAction() {
        return schedulingAction;
    }

    /**
     * Set the action performed at scheduling. Valid values are DELETE, PURGE, COPY (it can combined using comma as separator).
     *
     * @param schedulingAction the action performed at scheduling (DELETE, PURGE, COPY).
     */
    public void setSchedulingAction(String schedulingAction) {
        this.schedulingAction = schedulingAction;
    }

    /**
     * Get the pool size for the repository Maven servlet.
     *
     * @return the pool size.
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Set the pool size for the repository Maven servlet.
     *
     * @param poolSize the pool size.
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
