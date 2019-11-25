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

Apache Karaf Cave provides three different services:

* Artifact repositories manager
* Karaf features gateway
* Deployer

## Getting Started

For an Apache Karaf Cave source distribution, please read BUILDING.md for instructions on building Apache Karaf Cave.

Before using Apache Karaf Cave services, you have to register the Karaf Cave features repository. For example, on a running Apache Karaf instance:

```
karaf@root()> feature:repo-add cave 4.2.1
```

### Artifact repositories manager

Cave Repository service is a complete artifacts repository manager, supporting Maven, OSGi Bundle Repository and HTTP wrapping.

You can install the artifact repositories manager service with:

```
karaf@root()> feature:install cave-repository
```

You now have the service running. You can manipulate the artifact repositories using the `cave:repository-*` shell commands.

For details, take a look on the Apache Karaf Cave documentation.

### Karaf features gateway

Cave Karaf Features Gatewey is able to gather several features repositories in one, providing a single repository containing the union of all features.

You can install the gateway service with:

```
karaf@root()> feature:install cave-features-gateway
```

You now have the service running. You can manipulate the gateway using the `cave:features-gateway-*` shell commands.

For details, take a look on the Apache Karaf Cave documentation.

### Deployer

Cave Deployer allows you to control and provision a farm of Apache Karaf instances (deploying features, application, etc).

You can install the deployer service with:

```
karaf@root()> feature:install cave-deployer
```

You now have the service running. You can manipulate the deployer using the `cave:deployer-*` shell commands.

For details, take a look on the Apache Karaf Cave documentation.

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
