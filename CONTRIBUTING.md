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
Contributing to the hygieia-build-bamboo-collector.
=====================================================

You have found a bug or you have an idea for a cool new feature? Contributing code is a great way to
give something back to the Hygieia Build Bamboo Collector. Before you dig right into the code there 
are a few guidelines that we need contributors to follow so that we can have a chance of keeping on 
top of things.

Agreement and Conduct
---------------------

We welcome Your interest in Capital One’s Open Source Projects (the “Project”). Any Contributor to the Project 
must accept and sign an Agreement indicating agreement to the license terms below. Except for the license granted 
in this Agreement to Capital One and to recipients of software distributed by Capital One, You reserve all 
right, title, and interest in and to Your Contributions; this Agreement does not impact Your rights to use Your 
own Contributions for any other purpose.

* [Individual Agreement](https://docs.google.com/forms/d/19LpBBjykHPox18vrZvBbZUcK6gQTj7qv1O5hCduAZFU/viewform)
* [Corporate Agreement](https://docs.google.com/forms/d/e/1FAIpQLSeAbobIPLCVZD_ccgtMWBDAcN68oqbAJBQyDTSAQ1AkYuCp_g/viewform?usp=send_form)

This project adheres to the [Open Code of Conduct](https://developer.capitalone.com/single/code-of-conduct/). By 
participating, you are expected to honor this code.

Getting Started
---------------

### Developent Environment

For the most part, we are mererly concerned that you have [java](http://openjdk.java.net/) and 
[maven](https://maven.apache.org/) installed in your project. For the sake of testing the project, we
suggest that you regularly run: 

```
mvn clean test package site
```

after which you navigate to the project's `./target/site/index.html`, which you view in your browser. 
From here, you can view the project's reports in the left side navigation listed under 
"Project Documentation > Project Reports." Please ensure that your code does not add any new issues to the
reports and is sufficiently documented. 


### Issue tracking

+ Make sure you're filimlar with the GitHub issue tracking system. Our issues page is available
here: [https://github.com/Hygieia/hygieia-build-bamboo-collector/issues](https://github.com/Hygieia/hygieia-build-bamboo-collector/issues).
+ Submit a ticket for your issue, assuming one does not already exist.
  + Clearly describe the issue including steps to reproduce when it is a bug.
  + Make sure you fill in the earliest version that you know has the issue.
  + Make sure you add the appropriate labels on the right hand side of the issue for tracking purposes.
+ Fork the repository on GitHub.

Making Changes
--------------

+ Create a topic branch from where you want to base your work (this is usually the master branch).
+ Make commits of logical units.
+ In your commit message make sure it starts with `#XXX -` where "`#XXX`" represents the number of the github issue you created for tracking purposes. Note the syntax that you can use follows:
    + fix #xxx
    + fixes #xxx
    + fixed #xxx
    + close #xxx
    + closes #xxx
    + closed #xxx
    + resolve #xxx
    + resolves #xxx
+ resolved #xxx
+ Respect the original code style:
  + Only use spaces for indentation.
  + Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.
  + Check for unnecessary whitespace with git diff --check before committing.
+ Make sure your commit messages are in the proper format. Your commit message should contain the key of the GitHub issue.
+ Make sure you have added the necessary tests for your changes.
+ Run all the tests and checks with `mvn clean verify` (or more simply, just `mvn`) to assure nothing else was accidentally broken.

Making Trivial Changes
----------------------

For changes of a trivial nature to comments and documentation, it is not always necessary to create a new issue in GitHub.
In this case, it is appropriate to start the first line of a commit with '(doc)' instead of a ticket number.

Submitting Changes
------------------

+ Push your changes to a topic branch in your fork of the repository.
+ Submit a pull request to the repository in the __Hygieia__ organization.
+ Update your GitHub issue and include a link to the pull request in the ticket.

Additional Resources (Note these are external for informational purposes)
-------------------------------------------------------------------------

+ [General GitHub documentation](https://help.github.com/)
+ [GitHub pull request documentation](https://help.github.com/send-pull-requests/)
