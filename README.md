<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# Apache Karaf Cave

Apache Karaf Cave is an Apache Karaf subproject. It provides a complete repository manager and deployer for Apache Karaf.

## Overview

Apache Karaf Cave supports:

* Maven repository management
* OSGi Repository management
* Deployer on multiple Apache Karaf instances
* Complete REST API and JMX MBeans

Apache Karaf Cave provides the following features:
* Storage: Cave includes a storage backend. The default one is a simple filesystem backend. As the Cave backend
is designed in a plugin way, you can implement your own backend (for instance, JDBC or LDAP backend).
* Repository Metadata Generation: Cave creates the repository metadata for you, using the artifacts presents in the
repository storage.
* Maven support: Cave repositories act as a complete Maven repository, allowing you to use Cave directly with Maven.
* REST API: Cave provides a REST API to manipulate the repositories.
* Artifact Upload: Users can upload OSGi bundle in a Cave repository. It supports URLs like mvn:groupId/artifactId/version,
file:, http:, etc.
* Deployer: to deploy and manage your "farm" of Apache Karaf instances.
* Repository proxy: Cave is able to proxy an existing repository, for instance an existing Maven repository.
The artifacts are located on the "external" repository, Cave handles the repository metadata. Cave supports file: and http:
URLs, it means that Cave is able to browse a remote HTTP Maven repository for instance.
* Repository population: Cave is able to get artifacts present on an "external" repository (local file: or
remote http:), looking for OSGi bundles, and copy the artifacts in the Cave repository storage.

## Getting Started

For an Apache Karaf Cave source distribution, please read
BUILDING.md for instructions on building Apache Karaf Cave.

To install Apache Karaf Cave, first you have to register the Cave features descriptor:

```
karaf@root()> feature:repo-add mvn:org.apache.karaf.cave/apache-karaf-cave/4.0.0/xml/features
```

Now, you can install the Cave simply by typing:

```
karaf@root()> feature:install cave
```

## More Information

The PDF manual is the right place to find any information about Karaf Cave.

Alternatively, you can also find out how to get started here:
    http://karaf.apache.org/subprojects/cave

If you need more help try talking to us on our mailing lists
    http://karaf.apache.org/site/mailinglists.html

If you find any issues with Apache Karaf, please submit reports
with JIRA here:
    http://issues.apache.org/jira/browse/KARAF

We welcome contributions, and encourage you to get involved in the
Karaf community. If you'd like to learn more about how you can
contribute, please see:
    http://karaf.apache.org/index/community/contributing.html

Many thanks for using Apache Karaf Cave.

**The Apache Karaf Team**
