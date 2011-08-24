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

import com.sun.org.omg.CORBA.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;
import org.apache.karaf.cave.server.backend.api.CaveRepository;
import org.apache.karaf.cave.server.backend.api.CaveRepositoryService;
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
import java.rmi.server.UnicastRemoteObject;

/**
 * Wrapper servlet which "expose" Karaf Cave repository resources in HTTP.
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

        // lookup on the OBR RepositoryAdmin service
        ServiceReference repositoryAdminReference = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        if (repositoryAdminReference == null) {
            throw new ServletException("OBR repository admin service is not available");
        }
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(repositoryAdminReference);
        if (repositoryAdmin == null) {
            bundleContext.ungetService(repositoryAdminReference);
            throw new ServletException("OBR repository admin service is not available");
        }

        String uri = request.getPathInfo();

        // remove the starting /
        uri = uri.substring(1);

        try {
            URL url = null;

            if (uri.endsWith("repository.xml")) {
                // the user wants to get the Cave repository repository.xml

                ServiceReference caveRepositoryAdminReference = bundleContext.getServiceReference(CaveRepositoryService.class.getName());
                if (caveRepositoryAdminReference != null) {
                    CaveRepositoryService caveRepositoryService = (CaveRepositoryService) bundleContext.getService(caveRepositoryAdminReference);
                    if (caveRepositoryService != null) {
                        CaveRepository caveRepository = caveRepositoryService.getRepository(uri);
                        url = caveRepository.getRepositoryXml();
                        response.setContentType("text/xml");
                    }
                    bundleContext.ungetService(caveRepositoryAdminReference);
                }
            } else {
                Resource[] resources = repositoryAdmin.discoverResources("(uri=*" + uri + ")");
                if (resources.length == 0) {
                    throw new ServletException("No resource found with URI " + uri);
                }
                if (resources.length > 1) {
                    throw new ServletException("Multiple resources found with URI " + uri);
                }
                // I have exactly one resource associated to the URI
                url = new URL(resources[0].getURI());
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
        } finally {
            bundleContext.ungetService(repositoryAdminReference);
        }
    }

}
