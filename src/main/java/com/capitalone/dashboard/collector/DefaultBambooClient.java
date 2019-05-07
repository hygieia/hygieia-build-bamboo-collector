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

package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.BambooJob;
import com.capitalone.dashboard.model.Build;
import com.capitalone.dashboard.model.BuildStatus;
import com.capitalone.dashboard.model.SCM;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * BambooClient implementation that uses RestTemplate and JSONSimple to
 * fetch information from Bamboo instances.
 */
@Component
public class DefaultBambooClient implements BambooClient {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultBambooClient.class);

  private final RestOperations rest;
  private final BambooSettings settings;

  private static final String JOBS_URL_SUFFIX =
      "rest/api/latest/plan?expand=plans&max-result=2000";
  private static final String JOBS_RESULT_SUFFIX =
      "rest/api/latest/result/";
  private static final String BUILD_DETAILS_URL_SUFFIX =
      "?expand=results.result.artifacts&expand=changes.change.files";

  /**
   * Spring dependency-injection controller.
   *
   * @param restOperationsSupplier @Autowired.
   * @param settings               @Autowired.
   */
  @Autowired
  public DefaultBambooClient(
      Supplier<RestOperations> restOperationsSupplier,
      BambooSettings settings) {
    this.rest = restOperationsSupplier.get();
    this.settings = settings;
  }

  @SuppressWarnings("PMD.ExcessiveMethodLength")
  @Override
  public Map<BambooJob, Set<Build>> getInstanceJobs(String instanceUrl) {
    Map<BambooJob, Set<Build>> result = new LinkedHashMap<>();
    try {
      String url = joinUrl(instanceUrl, JOBS_URL_SUFFIX);
      ResponseEntity<String> responseEntity = makeRestCall(url);
      String returnJson = responseEntity.getBody();
      LOG.debug(returnJson);
      JSONParser parser = new JSONParser();

      try {
        JSONObject object = (JSONObject) parser.parse(returnJson);

        for (Object job : getJsonArray((JSONObject) object.get("plans"), "plan")) {
          JSONObject jsonJob = (JSONObject) job;

          final String planName = getString(jsonJob, "key");
          JSONObject link = (JSONObject) jsonJob.get("link");
          final String planUrl = getString(link, "href");

          LOG.debug("Plan:" + planName);
          LOG.debug("PlanURL: " + planUrl);

          // In terms of Bamboo this is the plan not job
          BambooJob bambooJob = new BambooJob();
          bambooJob.setInstanceUrl(instanceUrl);
          bambooJob.setJobName(planName);
          bambooJob.setJobUrl(planUrl);

          // Finding out the results of the top-level plan

          String resultUrl = joinUrl(instanceUrl, JOBS_RESULT_SUFFIX);
          resultUrl = joinUrl(resultUrl, planName);
          LOG.debug("Job:" + planName);
          LOG.debug("Result URL:" + resultUrl);
          responseEntity = makeRestCall(resultUrl);
          returnJson = responseEntity.getBody();
          LOG.debug("Result :" + returnJson);
          jsonJob = (JSONObject) parser.parse(returnJson);

          Set<Build> builds = new LinkedHashSet<>();
          for (Object build : getJsonArray((JSONObject) jsonJob.get("results"), "result")) {
            JSONObject jsonBuild = (JSONObject) build;
            LOG.debug("Entered each build for job : " + planName);
            // A basic Build object. This will be fleshed out later if this is a new Build.
            String dockerLocalHostIp = settings.getDockerLocalHostIP();
            String buildNumber = jsonBuild.get("buildNumber").toString();
            if (!"0".equals(buildNumber)) {
              LOG.debug("BuildNO " + buildNumber + " for planName: " + planName);
              Build bambooBuild = new Build();
              bambooBuild.setNumber(buildNumber);
              String buildUrl = joinUrl(resultUrl, buildNumber); //getString(jsonBuild, "url");
              LOG.debug(buildUrl);
              //Modify localhost if Docker Natting is being done
              if (!dockerLocalHostIp.isEmpty()) {
                buildUrl = buildUrl.replace("localhost", dockerLocalHostIp);
                LOG.debug("Adding build & Updated URL to map LocalHost for Docker: " + buildUrl);
              } else {
                LOG.debug(" Adding Build: " + buildUrl);
              }

              bambooBuild.setBuildUrl(buildUrl);
              builds.add(bambooBuild);
            }
          }
          // add the builds to the job
          result.put(bambooJob, builds);

          //But we might have many branches and subplans in them so we have to find them out as well
          String branchesUrl = joinUrl(planUrl, "/branch");
          responseEntity = makeRestCall(branchesUrl);
          returnJson = responseEntity.getBody();
          JSONObject jsonBranches = (JSONObject) parser.parse(returnJson);

          for (Object branch : getJsonArray((JSONObject) jsonBranches.get("branches"), "branch")) {
            JSONObject branchObject = (JSONObject) branch;
            String subPlan = branchObject.get("key").toString();
            // Figure out nested jobs under the branches

            resultUrl = joinUrl(instanceUrl, JOBS_RESULT_SUFFIX);
            resultUrl = joinUrl(resultUrl, subPlan);
            LOG.debug("sub Plan:" + subPlan);
            LOG.debug("sub plan-Result URL:" + resultUrl);
            responseEntity = makeRestCall(resultUrl);
            returnJson = responseEntity.getBody();
            LOG.debug("Result :" + returnJson);
            jsonJob = (JSONObject) parser.parse(returnJson);

            for (Object build : getJsonArray((JSONObject) jsonJob.get("results"), "result")) {
              JSONObject jsonBuild = (JSONObject) build;
              LOG.debug("Entered each build for nested plan : " + subPlan);
              // A basic Build object. This will be fleshed out later if this is a new Build.
              String dockerLocalHostIp = settings.getDockerLocalHostIP();
              String buildNumber = jsonBuild.get("buildNumber").toString();
              if (!"0".equals(buildNumber)) {
                LOG.debug("BuildNO " + buildNumber + " for planName: " + planName);
                Build bambooBuild = new Build();
                bambooBuild.setNumber(buildNumber);
                String buildUrl = joinUrl(resultUrl, buildNumber); //getString(jsonBuild, "url");
                LOG.debug(buildUrl);
                //Modify localhost if Docker Natting is being done
                if (!dockerLocalHostIp.isEmpty()) {
                  buildUrl = buildUrl.replace("localhost", dockerLocalHostIp);
                  LOG.debug("Adding build & Updated URL to map LocalHost for Docker: " + buildUrl);
                } else {
                  LOG.debug(" Adding Build: " + buildUrl);
                }

                bambooBuild.setBuildUrl(buildUrl);
                builds.add(bambooBuild);
              }
            }
            // add the builds to the job
            result.put(bambooJob, builds);

            // Ended with nested branches
          }

        }
      } catch (ParseException parseException) {
        LOG.error("Parsing jobs on instance: " + instanceUrl, parseException);
      }
    } catch (RestClientException restClientException) {
      LOG.error("client exception loading jobs", restClientException);
      throw restClientException;
    } catch (MalformedURLException malformedUrlException) {
      LOG.error("malformed url for loading jobs", malformedUrlException);
    }
    return result;
  }

  @Override
  public Build getBuildDetails(String buildUrl, String instanceUrl) {
    try {
      String newUrl = rebuildJobUrl(buildUrl, instanceUrl);
      String url = joinUrl(newUrl, BUILD_DETAILS_URL_SUFFIX);
      LOG.debug("Build Details URL:" + url);
      ResponseEntity<String> result = makeRestCall(url);
      String resultJson = result.getBody();
      LOG.debug("Build Details :" + resultJson);
      if (StringUtils.isEmpty(resultJson)) {
        LOG.error("Error getting build details for. URL=" + url);
        return null;
      }
      JSONParser parser = new JSONParser();
      try {
        JSONObject buildJson = (JSONObject) parser.parse(resultJson);
        Boolean finished = (Boolean) buildJson.get("finished");
        // Ignore jobs that are building
        if (finished) {
          Build build = new Build();
          build.setNumber(buildJson.get("buildNumber").toString());
          build.setBuildUrl(buildUrl);
          build.setTimestamp(System.currentTimeMillis());

          //"2016-06-23T09:13:29.961+07:00"
          SimpleDateFormat dateFormat =
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
          Date parsedDate = dateFormat.parse(buildJson.get("buildStartedTime").toString());
          build.setStartTime((Long) parsedDate.getTime());

          build.setDuration((Long) buildJson.get("buildDuration"));
          build.setEndTime(build.getStartTime() + build.getDuration());
          build.setBuildStatus(getBuildStatus(buildJson));
          if (settings.isSaveLog()) {
            build.setLog(getLog(buildUrl));
          }
          addChangeSets(build, buildJson);
          return build;
        }

      } catch (Exception exception) {
        LOG.error("Parsing build: " + buildUrl, exception);
      }
    } catch (RestClientException rce) {
      LOG.error("Client exception loading build details: "
          + rce.getMessage() + ". URL =" + buildUrl);
    } catch (MalformedURLException mfe) {
      LOG.error("Malformed url for loading build details"
          + mfe.getMessage() + ". URL =" + buildUrl);
    } catch (URISyntaxException use) {
      LOG.error("Uri syntax exception for loading build details"
          + use.getMessage() + ". URL =" + buildUrl);
    } catch (RuntimeException re) {
      LOG.error("Unknown error in getting build details. URL=" + buildUrl, re);
    } catch (UnsupportedEncodingException unse) {
      LOG.error("Unsupported Encoding Exception in getting build details. URL="
          + buildUrl, unse);
    }
    return null;
  }


  /**
   * This method will rebuild the API endpoint because the buildUrl obtained via Jenkins API
   * does not save the auth user info and we need to add it back.
   * @param build the {@link String} representing the build.
   * @param server the {@link String} representing the server.
   * @return the job url.
   * @throws URISyntaxException if we get an exception building the url.
   * @throws MalformedURLException if we get an exception building the url.
   * @throws UnsupportedEncodingException if we get an exception building the url.
   */
  public static String rebuildJobUrl(String build, String server)
      throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
    URL instanceUrl = new URL(server);
    String userInfo = instanceUrl.getUserInfo();
    String instanceProtocol = instanceUrl.getProtocol();

    //decode to handle spaces in the job name.
    URL buildUrl = new URL(URLDecoder.decode(build, "UTF-8"));
    String buildPath = buildUrl.getPath();

    String host = buildUrl.getHost();
    int port = buildUrl.getPort();
    URI newUri = new URI(instanceProtocol, userInfo, host, port, buildPath, null, null);
    return newUri.toString();
  }


  /**
   * Grabs changeset information for the given build.
   *
   * @param build     a Build
   * @param buildJson the build JSON object
   */
  private void addChangeSets(Build build, JSONObject buildJson) {
    JSONObject changeSet = (JSONObject) buildJson.get("changes");

    for (Object item : getJsonArray(changeSet, "change")) {
      JSONObject jsonItem = (JSONObject) item;
      SCM scm = new SCM();
      scm.setScmAuthor(getString(jsonItem, "author"));
      scm.setScmCommitLog(getString(jsonItem, "comment"));
      scm.setScmCommitTimestamp(getCommitTimestamp(jsonItem));
      scm.setScmRevisionNumber(getRevision(jsonItem));
      scm.setScmUrl(getString(jsonItem, "commitUrl"));
      scm.setNumberOfChanges(getJsonArray((JSONObject) jsonItem.get("files"), "file").size());
      build.getSourceChangeSet().add(scm);
    }
  }

  ////// Helpers

  private long getCommitTimestamp(JSONObject jsonItem) {
    if (jsonItem.get("timestamp") != null) {
      return (Long) jsonItem.get("timestamp");
    } else if (jsonItem.get("date") != null) {
      String dateString = (String) jsonItem.get("date");
      try {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(dateString).getTime();
      } catch (java.text.ParseException parseException) {
        // Try an alternate date format...looks like this one is used by Git
        try {
          return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(dateString).getTime();
        } catch (java.text.ParseException e1) {
          LOG.error("Invalid date string: " + dateString, parseException);
        }
      }
    }
    return 0;
  }

  private String getString(JSONObject json, String key) {
    return (String) json.get(key);
  }

  private String getRevision(JSONObject jsonItem) {
    // Use revision if provided, otherwise use id
    Long revision = (Long) jsonItem.get("revision");
    return revision == null ? getString(jsonItem, "changesetId") : revision.toString();
  }

  private JSONArray getJsonArray(JSONObject json, String key) {
    Object array = json.get(key);
    return array == null ? new JSONArray() : (JSONArray) array;
  }

  private String firstCulprit(JSONObject buildJson) {
    JSONArray culprits = getJsonArray(buildJson, "culprits");
    if (CollectionUtils.isEmpty(culprits)) {
      return null;
    }
    JSONObject culprit = (JSONObject) culprits.get(0);
    return getFullName(culprit);
  }

  private String getFullName(JSONObject author) {
    return getString(author, "fullName");
  }

  private String getCommitAuthor(JSONObject jsonItem) {
    // Use user if provided, otherwise use author.fullName
    JSONObject author = (JSONObject) jsonItem.get("author");
    return author == null ? getString(jsonItem, "user") : getFullName(author);
  }

  private BuildStatus getBuildStatus(JSONObject buildJson) {
    String status = buildJson.get("buildState").toString();
    switch (status) {
      case "Successful":
        return BuildStatus.Success;
      case "UNSTABLE":
        return BuildStatus.Unstable;
      case "Failed":
        return BuildStatus.Failure;
      case "ABORTED":
        return BuildStatus.Aborted;
      default:
        return BuildStatus.Unknown;
    }
  }

  protected ResponseEntity<String> makeRestCall(String url) throws MalformedURLException {
    URI thisUri = URI.create(url);
    String userInfo = thisUri.getUserInfo();

    //get userinfo from URI or settings (in spring properties)
    if (StringUtils.isEmpty(userInfo) && (this.settings.getUsername() != null) && (this.settings.getApiKey() != null)) {
      userInfo = this.settings.getUsername() + ":" + this.settings.getApiKey();
    }
    // Basic Auth only.
    if (StringUtils.isNotEmpty(userInfo)) {
      return rest.exchange(thisUri, HttpMethod.GET,
          new HttpEntity<>(createHeaders(userInfo)),
          String.class);
    } else {
      return rest.exchange(thisUri, HttpMethod.GET, null,
          String.class);
    }

  }

  protected HttpHeaders createHeaders(final String userInfo) {
    byte[] encodedAuth = Base64.encodeBase64(
        userInfo.getBytes(StandardCharsets.US_ASCII));
    String authHeader = "Basic " + new String(encodedAuth);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
    headers.set(HttpHeaders.ACCEPT, "application/json");
    return headers;
  }

  protected String getLog(String buildUrl) {
    try {
      return makeRestCall(joinUrl(buildUrl, "consoleText")).getBody();
    } catch (MalformedURLException mfe) {
      LOG.error("malformed url for build log", mfe);
    }

    return "";
  }

  // join a base url to another path or paths - this will handle trailing or non-trailing /'s
  public static String joinUrl(String base, String... paths) throws MalformedURLException {
    StringBuilder result = new StringBuilder(base);
    for (String path : paths) {
      String p = path.replaceFirst("^(\\/)+", "");
      if (result.lastIndexOf("/") != result.length() - 1) {
        result.append('/');
      }
      result.append(p);
    }
    return result.toString();
  }
}
