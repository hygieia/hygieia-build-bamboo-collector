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

Development Documentation.
==========================

#### Other documentation in this area

* [GnuPG Signing](https://github.com/Hygieia/hygieia-scm-subversion-collector/blob/master/src/docs/gpg-signing.md) - A prerequisite for releasing to Maven Central.


### Prerequisites and sanity checks.

It is suggested that you have:

* [Java 8](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html)
* [Maven 3.6.X](https://maven.apache.org/download.cgi)

installed before attempting to build the project. For the sake of building, testing and seeing the project reports, it 
is suggested that you regularly run `mvn clean test install site` and view the maven generated site that gets built into 
`${basedir}/target/site` (navigating to the `index.html` in here will serve the static site locally), where `${basedir}` 
represents the directory containing the project's root locally cloned from github. Note, the site should look analogous to 
https://hygieia.github.io/hygieia-build-bamboo-collector/ the github page.

### Maven plugins.

We have a variety of plugins installed for generating reports. Notice the [reports page on the site](https://hygieia.github.io/hygieia-build-bamboo-collector/project-reports.html) it has:

* javadoc
* test javadoc
* [checkstyle](https://maven.apache.org/plugins/maven-checkstyle-plugin/) - static analysis that validates the google style guide; helpful for readability
  * Note, the `checkstyle.xml` (the rules that govern the projects output) resides in `src/devops/checkstyle.xml` and is essentially 
    verbatim the google style guide.
* [jacoco](https://www.eclemma.org/jacoco/trunk/doc/maven.html) - code coverage. This is indeed important. We like to see numbers greater than 90% and are happy when we surpass 95%.
* [japicmp](http://siom79.github.io/japicmp/) - a tool for doing java API comparison, so that we can see what changes between versions. 
  This tool will tell us when we've made "binary compatibility" (BC for short) non-breaking or breaking changes. When we break BC, it is 
  highly advised that the version of the component be major version number upgraded.
* [rat plugin](http://creadur.apache.org/rat/apache-rat-plugin/) - the Apache "RAT" plugin validates that the Apache 2.0 License header is
  included in every individual file in the project. Note that every file is a released artifact as it is served by github. Thus, every file 
  need be individually licensed. Note, we accommodate for exceptions to this rule when files contain data that necesarily resides in a 
  format that precludes comments (e.g. JSON or CSV). Much like with the japicmp plugin, we highly suggest that the rules of licensing be 
  adhered to
* spotbugs - static analysis. (take with a grain of salt, use it as a reason to re-read your code)
* pmd - static anslysis (take with a grain of salt, use it as a reason to re-read your code)
* [changes](https://maven.apache.org/plugins/maven-changes-plugin/) - a release notes generator assuming that the `src/changes/changes.xml` 
  is sufficiently maintained.
  * This plugin both generates a change report for the site as well as generating a `RELEASE-NOTES.txt` or `announcement.vm` if run properly: `mvn changes:announcement-generate`. Do read 
    the site as it contains substancially more information.
    
### Maintaining the `changes.xml`

As stipulated above, the `maven-changes-plugin`, gives us the mechnism to generate both the
release history for the maven site as well as generate `RELEASE-NOTES.txt`. Our configuration is
such that we use the github issues for tracking our changes. Notice in the 
[changes.xml](../changes/changes.xml), we have releases with individual `action`'s under them.
Each action should correspond to a github issue.

We're afforded the luxury now of having a variety of reports that can be built. For the 
site's changes report, all we need worry about is the `mvn clean install site` command, and
the report will get build into the site available at `./target/site/index.html`.

Regarding generating the `RELEASE-NOTES.txt`, we run `mvn changes:announcement-generate`, and
we get an `announcement.vm` file generated at `./target/announcement/announcement.vm`. We
suggest that the release manager take this file and append it to the beginning of 
`./RELEASE-NOTES.txt`, which if it does not exist, we suggest you create.


### Publishing the maven site to `gh-pages`.

Now that we have a maven site that get's generated, we need to have set up 
authenticaiton properly with github. Because of the way the Capital One proxy is setup
we will want to use a `~/.netrc` file and clone from a url that looks like 
`https://<githubId>@github.com/Hygieia/hygieia-build-bamboo-collector`. Your `~/.netrc` file should
look like:

```
machine github.com
login <github_username>
password <github_oauth_token>

machine api.github.com
login <github_username>
password <github_oauth_token>
```

With this in place you'll be able to use any github remote of the form `https://<github_username>@github.com/Org/Repo`,
and you will be authenticated with github at the command line by default now.

Once we set these permissions up, deploying the site from your local branch becomes releatively easy. You
merely run the following:

```
mvn -Dgithub.username=<github_username> clean test package site site-deploy`
```

This will clone down the `gh-pages` branch under a local subdirectory sibling to `target` named `site-content` 
(*note,* `site-content` is in the project's `.gitignore` so that we don't accidentally check in the site 
to the master branch). The maven build will then copy the `./target/site` directory in to `./site-content` and 
will perform a git push back up to the `gh-pages` branch, thus deploying the site.

### Releasing `hygieia-build-bamboo-collector`.

__Note.__ You need to have set up your gpg signing mechanics. The setup guide is in [GnuPG Signing](gpg-signing.md).

#### Setting up your maven settings.xml

You will need a maven `settings.xml` file with credentials for maven central. To retrieve this, you will need to
log into https://oss.sonatype.org with credentials retrieved from the committer team. There are two ways to do this: 
(1) use the existent system account, or (2) open a Jira ticket with sonatype to onboard your username to the 
staging target in the sonatype nexus instance. Then navigate to your username in the top right corner, and click
"Profile." You should see a window with your profile and there's a little dropdown with the word "Summary" in
it. Click on this and select "User Token." Click on the "Access User Token" button and re-enter the password. 
You should see something that looks like (__Note__: there has be substantive obvuscations done on the below 
credential set, so they should be invalid for our purposes):

```xml
<server>
  <id>${server}</id>
  <username>uA3vjikn</username>
  <password>wv845nj2qYaGb58MS5Kx7jehunfewdnoLX/1ij53REIOJ</password>
</server>
```

which can be pasted into your `settings.xml` file as credentials for the distribution servers listed in 
the [pom.xml](../../pom.xml). Substitute `ossrh` for `${server}` in the above `id` field. You further may need
to configure a `proxy` entry instructions for which can be found on [the Maven Project site's proxy 
documentation page](https://maven.apache.org/guides/mini/guide-proxies.html?).

##### Preparing for the release

1. Run the build to see that everything passes properly. Specifically, check the site to ensure that all
   requisite reports are clean.
2. Ensure that any references in the project's documentation that specify a version point to the release
   candidate version that we wish to create.
3. Checkout yourself onto a release branch either in your fork or upstream. We generally suggest that you
   name the branch the version that you intend to use for the release.
4. Upversion the pom.xml file to the version that you wish to create, ensuring that no `-SNAPSHOT` suffix is
   present. You can either do this manually or using the `maven-versions-plugin` by running 
   `mvn -DnewVersion=<theVersionYouWishToSet> -DgenerateBackupPoms=false versions:set`.
5. Generate the release notes using the `maven-release-plugin`.
   * Backup the `RELEASE-NOTES.txt` because the following command will overwrite it. We suggest you append the
     old release notes to the end of the newly generated `RELEASE-NOTES.txt` file. 
   * Run `mvn -Prelease-notes clean changes:announcement-generate`.
6. Ensure that everything above is committed to your release branch.
7. Create a git tag:
    ```bash
    $git tag -s bamboo-build-collector-<version_number>-RC<RC_number> -m "<your message>"
    ```
    where the `version_number` represents the version of the maven artifact that you are going to build and 
    `RC_number` represents the number of the release candidate that you are proposing to our community.
    * Github offer's a convenient documentation system in the tags area associated with adding 
      RELEASE-NOTES.txt like documentation. Consider the following RC "pre-release" tag: 
      https://github.com/Hygieia/hygieia-build-bamboo-collector/releases/tag/bamboo-build-collector-3.0.2-RC1. Notice
      that we've added all of the release artifacts as well as the release notes, and a link to
      the [VOTE] thread for the sake of posterity. We suggest that all future releases have such documentation.
8. Test your build using:
    ```bash
    $mvn -Duser.name=<your_github_username> -Prelease -Ptest-deploy clean test install site deploy
    ```
9. Stage your release using the `settings.xml` created above from the credentials for maven central. 
   We suggest that you not use these credentials in your normal `settings.xml` file as to ensure that you
   only stage artifacts that you choose to stage. The stanging command will end up being:
   ```bash
   $mvn -s <path_to_settings>/settings.xml -Duser.name=<your_github_username> -Prelease clean test package site deploy
   ```
   __Note.__ It is important that you not run `install` during this step as it will double the number of signatures.
10. This will create a staging repository in https://oss.sonatype.org. You can navigate to the staging repositories
    by logging in and navigating to "Staging Repositories," on the left navigation. The repository will be named
    `comcapitalone-####` and will be "open." You can select the check box and either "close" or "drop" it. 
    __Note,__ do not click "Release" until you are completely ready to promote the build to maven central!!!!
    Look through the staging repository to verify that everything in there is precisely what you want, and then
    "close" it.
11. Log into [gitter](https://gitter.im/capitalone/Hygieia), and post a "[VOTE]" in the following manner 
    (for version `3.0.2-RC1`):
    ```
    @/all - [VOTE] I propose we release the com.capitalone.dashboard:bamboo-build-collector:3.0.2 from RC1
    the git tag is here:
    
    https://github.com/Hygieia/hygieia-build-bamboo-collector/tree/bamboo-build-collector-3.0.2-RC1
    
    the staging repository is located at:
    
    https://oss.sonatype.org/content/repositories/comcapitalone-1136/com/capitalone/dashboard/bamboo-build-collector/3.0.2/
    
    Please review the release candidate (clone the repository, and build from the tag "mvn clean test install site" and vote accordingly.
    
    [ ] +1 Release these artifacts
    [ ] +0 OK, but...
    [ ] -0 OK, but really should fix...
    [ ] -1 I oppose this release because...
    ```
12. We suggest that some amount of time be laid out in the above [VOTE] before it "closes" in order
    to give sufficient time for a reasonable number of committers to validate the release. 

##### Promoting a release.

If you have recieved 3 `+1` votes and no `-1` votes from project committers, it is safe to continue with 
the release promotion process.

1. Announce to the gitter channel that you have tallied the votes and that you plan on 
   promoting the release. Make note of the original vote for the sake of posterity.
2. Log in to https://oss.sonatype.org, find the staging repository that you staged above, click it's 
   checkbox and click "release." For the sake of posterity, it's worth putting the [VOTE] thread's
   link in the comments. An example [VOTE] thread link follows as: https://gitter.im/capitalone/Hygieia/archives/2019/04/24
3. Check out the RC tag from above and run the following git command:
   ```bash
   $git tag -s bamboo-build-collector-<version_number> -m "<your message>"
   ```
   * Push this tag up to github and document it in a similar fashion as you did in the [Preparing 
     for the release](#preparing-for-the-release) section.
4. Checkout the tag you just created, and run
   ```bash
   $mvn -Dgithub.username=<your_github_username> clean test package site site-deploy
   ```
   * If this doesn't work, you may need to set up your [Publishing the 
     maven site to `gh-pages`](#publishing-the-maven-site-to-gh-pages) authentication properly.
5. Run `git checkout master`, then run `git merge bamboo-build-collector-<version_of_release>`
   and get all of the changes for the release on master.
   * Make sure that your next commit is to ensure that the [`pom.xml`](../../pom.xml)'s `<version>` is the
     next `-SNAPSHOT` you wish to release.
   * Go to the [`changes.xml`](../changes/changes.xml), and ensure that you create a new `release` section
     with no `actions` under the next version that you wish to use.
   * Push all of your post release changes to `master` back up to the repository.
6. Go to the project's [milestones](https://github.com/Hygieia/hygieia-build-bamboo-collector/milestones),
   and ensure that the milestone related to the release that just went out get's closed and referenced
   in the release tag. Furthermore, ensure that there is a milestone for the next version.
7. __Wait for the release to show up in maven central.__
8. Go out to [gitter](https://gitter.im/capitalone/Hygieia) and announce the release.