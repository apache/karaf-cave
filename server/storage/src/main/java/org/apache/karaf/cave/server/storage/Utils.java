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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.jsoup.Connection;

public class Utils {

    public static void deleteRecursive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Returns the <code>Properties</code> object as represented by the
     * property list (key and element pairs) in the file path
     * at the given <code<>propertiesFile</code>.
     *
     * @param   propertiesFile a Properties file containing key-element pairs.
     * @return  a <code>Properties</code> object containing the properties read from the given file.
     * @throws  IOException if an error occurred when reading from the input stream.
     */
    public static Properties loadProperties (String propertiesFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        return properties;
    }

    /**
     * An instance of the <code>Authorizer</code> class
     * sets the <code>HttpHeaders.AUTHORIZATION</code> request.
     */
    public static class Authorizer {

        // Property keys expected in the Properties file.
        private static final String HTTP_USERNAME = "cave.storage.http.username";
        private static final String HTTP_PASSWORD = "cave.storage.http.password";

        // a Properties object to store the contents of the Properties file.
        private Properties properties;

        /**
         * Instantiates an object of the <code>Authorizer</code> class
         * with the given <code>propertiesFile</code>.
         *
         * @param   propertiesFile a Properties file containing key-element pairs.
         * @throws  IOException if an error occurred when reading from the input stream.
         */
        public Authorizer (String propertiesFile) throws IOException {
            this.properties = Utils.loadProperties(propertiesFile);
        }

        /**
         * Returns the given <code>HttpURLConnection</code> object
         * modified by setting the <code>HttpHeaders.AUTHORIZATION</code> header
         * depending on whether or not the authorization keys have been set.
         *
         * @param   connection an instance of <code>HttpURLConnection</code>.
         * @return  a modified <code>HttpURLConnection</code> object.
         */
        public HttpURLConnection authorize(HttpURLConnection connection) {
            if (containsAuthorizationKeys()) {
                connection.setRequestProperty(HttpHeaders.AUTHORIZATION, getAuthorizationHeader());
            }
            return connection;
        }

        /**
         * Returns the given <code>URLConnection</code> object
         * modified by setting the <code>HttpHeaders.AUTHORIZATION</code> header
         * depending on whether or not the authorization keys have been set.
         *
         * @param   connection an instance of <code>URLConnection</code>.
         * @return  a modified <code>URLConnection</code> object.
         */
        public URLConnection authorize(URLConnection connection) {
            if (containsAuthorizationKeys()) {
                connection.setRequestProperty(HttpHeaders.AUTHORIZATION, getAuthorizationHeader());
            }
            return connection;
        }

        /**
         * Returns the given <code>Connection</code> object
         * modified by setting the <code>HttpHeaders.AUTHORIZATION</code> header
         * depending on whether or not the authorization keys have been set.
         *
         * @param   connection an instance of <code>Connection</code>.
         * @return  a modified <code>Connection</code> object.
         */
        public Connection authorize(Connection connection) {
            if (containsAuthorizationKeys()) {
                connection.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader());
            }
            return connection;
        }

        /**
         * Returns <code>true</code>, iff the <code>Properties</code> file
         * contains the required Authorization keys.
         *
         * @return  <code>true</code> iff the Authorization keys are present.
         */
        private boolean containsAuthorizationKeys() {
            return properties.containsKey(HTTP_USERNAME) && properties.containsKey(HTTP_PASSWORD);
        }

        /**
         * Returns the <code>String</code> representation
         * of the Authorization header.
         *
         * @return  the Authorization header.
         */
        private String getAuthorizationHeader () {
            return getAuthorizationHeader(
                    properties.getProperty(HTTP_USERNAME),
                    properties.getProperty(HTTP_PASSWORD)
            );
        }

        /**
         * Returns the encoded <code>String</code> representation
         * of the Authorization header for the given username and password.
         *
         * @param   username the username for the <code>HttpHeaders.AUTHORIZATION</code> header.
         * @param   password the password for the <code>HttpHeaders.AUTHORIZATION</code> header.
         * @return  an encoded <code>String</code> representation for Authorization.
         */
        private String getAuthorizationHeader (String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
            return "Basic " + new String(encodedAuth);
        }
    }
}
