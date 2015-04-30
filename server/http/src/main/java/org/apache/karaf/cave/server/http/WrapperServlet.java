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
package org.apache.karaf.cave.server.http;

import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Wrapper servlet which "exposes" Karaf Cave repository resources in HTTP.
 */
public class WrapperServlet extends HttpServlet {

    private BundleContext bundleContext;

    public void init(ServletConfig servletConfig) throws ServletException {
        ServletContext context = servletConfig.getServletContext();
        bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ServiceReference caveRepositoryServiceReference = bundleContext.getServiceReference(CaveRepositoryService.class.getName());
        if (caveRepositoryServiceReference == null) {
            throw new ServletException("CaveRepositoryService is not available");
        }
        CaveRepositoryService caveRepositoryService = (CaveRepositoryService) bundleContext.getService(caveRepositoryServiceReference);
        if (caveRepositoryService == null) {
            throw new ServletException("CaveRepositoryService is not available");
        }

        try {
            doIt2(caveRepositoryService, request, response);
        } finally {
            bundleContext.ungetService(caveRepositoryServiceReference);
        }
    }

    private void doIt2(CaveRepositoryService caveRepositoryService, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String uri = request.getPathInfo();

        // remove the starting /
        uri = uri.substring(1);

        // listing the repositories
        if (request.getParameter("repositories") != null) {
            CaveRepository[] caveRepositories = caveRepositoryService.getRepositories();
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            for (CaveRepository caveRepository : caveRepositories) {
                writer.println(caveRepository.getName());
            }
            writer.flush();
            writer.close();
            return;
        }

        // wrapping content (repository.xml or directly artifacts)
        try {
            URL url = null;

            if (uri.endsWith("-repository.xml")) {
                // the user wants to get the Cave repository repository.xml
                // the expected format is {cave-repo-name}-repository.xml
                int index = uri.indexOf("-repository.xml");
                String caveRepositoryName = uri.substring(0, index);

                CaveRepository caveRepository = caveRepositoryService.getRepository(caveRepositoryName);
                if (caveRepository == null) {
                    throw new ServletException("No repository found for name " + caveRepositoryName);
                }
                url = caveRepository.getRepositoryXml();
                response.setContentType("text/xml");
            } else {
                for (CaveRepository repository : caveRepositoryService.getRepositories()) {
                    URL resourceUrl = repository.getResourceByUri(uri);
                    if (resourceUrl != null) {
                        if (url != null) {
                            throw new ServletException("Multiple resources found with URI " + uri);
                        } else {
                            url = resourceUrl;
                        }
                    }
                }
                if (url == null) {
                    throw new ServletException("No resource found with URI " + uri);
                }
                response.setContentType("application/java-archive");
            }

            // send the resource content to the HTTP response
            InputStream inputStream = url.openStream();
            OutputStream outputStream = response.getOutputStream();
            int c;
            while ((c = inputStream.read()) >= 0) {
                outputStream.write(c);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();

        } catch (ServletException servletException) {
            throw servletException;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
