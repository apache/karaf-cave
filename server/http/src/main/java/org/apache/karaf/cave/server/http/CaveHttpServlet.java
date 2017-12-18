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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.karaf.cave.server.api.CaveFeatureGateway;
import org.apache.karaf.cave.server.api.CaveRepository;
import org.apache.karaf.cave.server.api.CaveRepositoryService;
import org.apache.karaf.util.XmlUtils;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Wrapper servlet which "exposes" Karaf Cave repository resources in HTTP.
 */
public class CaveHttpServlet extends HttpServlet {

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String GZIP = "gzip";

    private BundleContext bundleContext;
    private ServiceTracker<CaveRepositoryService, CaveRepositoryService> tracker;

    public void init(ServletConfig servletConfig) throws ServletException {
        ServletContext context = servletConfig.getServletContext();
        bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
        tracker = new ServiceTracker<>(bundleContext, CaveRepositoryService.class, null);
        tracker.open();
    }

    @Override
    public void destroy() {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
        super.destroy();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        String uri = request.getPathInfo();
        // remove the starting /
        if (uri != null) {
            uri = uri.substring(1);
            if (request.getPathInfo().endsWith("-repository.xml")) {
                // the user wants to get the Cave repository repository.xml
                // the expected format is {cave-repo-name}-repository.xml
                int index = uri.indexOf("-repository.xml");
                String caveRepositoryName = uri.substring(0, index);
                CaveRepositoryService caveRepositoryService = tracker.getService();
                if (caveRepositoryService != null) {
                    CaveRepository caveRepository = caveRepositoryService.getRepository(caveRepositoryName);
                    if (caveRepository != null) {
                        return caveRepository.getIncrement();
                    }
                }
            }
        }
        return -1;
    }

    public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CaveRepositoryService caveRepositoryService = tracker.getService();
        if (caveRepositoryService == null) {
            throw new ServletException("CaveRepositoryService is not available");
        }
        doIt2(caveRepositoryService, request, response);
    }

    private void doIt2(CaveRepositoryService caveRepositoryService, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String uri = request.getPathInfo();

        // Cave Feature gateway
        if (uri.equals("/gateway")) {
            response.setContentType("application/xml");
            File file = new File(CaveFeatureGateway.STORAGE);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                PrintWriter writer = response.getWriter();
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
                writer.flush();
                writer.close();
            }
            return;
        }

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

                OutputStream os = response.getOutputStream();
                if (acceptsGZipEncoding(request)) {
                    os = new GZIPOutputStream(os);
                    response.addHeader("Content-Encoding", "gzip");
                }
                resolveRelativeUrls(url, request.getRequestURL().toString(), os);
                os.flush();
                os.close();
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
            }

        } catch (ServletException servletException) {
            throw servletException;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
        String acceptEncoding = httpRequest.getHeader(HEADER_ACCEPT_ENCODING);
        return acceptEncoding != null && acceptEncoding.contains(GZIP);
    }

    private void resolveRelativeUrls(URL url, String baseUri, OutputStream os) throws IOException, XMLStreamException, SAXException, ParserConfigurationException, TransformerException {
        // Read
        Document doc = XmlUtils.parse(url.toExternalForm());
        // Transform
        resolveUrls(doc, baseUri);
        // Output
        DOMSource src = new DOMSource(doc);
        StreamResult res = new StreamResult(os);
        XmlUtils.transform(src, res);
    }

    private void resolveUrls(Node node, String baseUri) {
        if (node != null) {
            if (node instanceof Element &&
                    node.getNodeName().equals("attribute")) {
                String name = ((Element) node).getAttribute("name");
                if ("url".equals(name)) {
                    String value = ((Element) node).getAttribute("value");
                    URI uri = URI.create(value);
                    if (!uri.isAbsolute()) {
                        uri = URI.create(baseUri).resolve(uri);
                        ((Element) node).setAttribute("value", uri.toASCIIString());
                    }
                }
            }
            resolveUrls(node.getFirstChild(), baseUri);
            resolveUrls(node.getNextSibling(), baseUri);
        }
    }

}
