<!---
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
Hygieia Build Bamboo Collector.
===============================

[![Coverage Status](https://coveralls.io/repos/github/Hygieia/hygieia-banbood-build/badge.svg?branch=master)](https://coveralls.io/github/Hygieia/hygieia-scm-subversion-collector?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.capitalone.dashboard/bambood-build-collector/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.capitalone.dashboard/bambood-build-collector/)

Configure the Bamboo Collector to display and monitor inf                                                                                                                                                                                                                                                                                                                                                                           ormation (related to build status) on the Hygieia Dashboard, from Bamboo. Hygieia uses Spring Boot to package the collector as an executable JAR file with dependencies.

### Setup Instructions

To configure the Bamboo Collector, execute the following steps:

*   **Step 1: Change Directory**

Change the current working directory to the `bamboo` directory of your Hygieia source code installation.

For example, in the Windows command prompt, run the following command:

```
cd C:\Users\[usernname]\hygieia\collectors\build\bamboo
```

*   **Step 2: Run Maven Build**

Run the maven build to package the collector into an executable JAR file:

``` 
mvn install
```

The output file `bamboo-collector.jar` is generated in the `bamboo\target` folder.

*   **Step 3: Set Parameters in Application Properties File**

Set the configurable parameters in the `application.properties` file to connect to the Dashboard MongoDB database instance, including properties required by the Bamboo Collector.

To configure parameters for the Bamboo Collector, refer to the sample [application.properties](#sample-application-properties-file) file.

For information about sourcing the application properties file, refer to the [Spring Boot Documentation](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-external-config-application-property-files).

*   **Step 4: Deploy the Executable File**

To deploy the `bamboo-collector.jar` file, change directory to `bamboo\target`, and then execute the following from the command prompt:

```
java -jar bamboo-collector.jar --spring.config.name=bamboo --spring.config.location=[path to application.properties file]
```

### Sample Application Properties File

```properties
# Database Name
dbname=dashboarddb

# Database HostName - default is localhost
dbhost=localhost

# Database Port - default is 27017
dbport=9999

# MongoDB replicaset
dbreplicaset=[false if you are not using MongoDB replicaset]
dbhostport=[host1:port1,host2:port2,host3:port3]

# Database Username - default is blank
dbusername=dashboarduser

# Database Password - default is blank
dbpassword=dbpassword

# Collector schedule (required)
bamboo.cron=0 0/5 * * * *

# Jenkins server (required) - Can provide multiple
bamboo.servers[0]=http://bamboo.company.com

# If using username/token for API authentication
# (required for Cloudbees Jenkins Ops Center) see sample
bamboo.servers[1]=http://username:token@bamboo.company.com

# Another option: If using same username/password Jenkins auth,
# set username/apiKey to use HTTP Basic Auth (blank=no auth)
bamboo.username=
bamboo.apiKey=

# Determines if build console log is collected - defaults to false
# (Bamboo for some reason has not exposed it as an API)
bamboo.saveLog=false
```