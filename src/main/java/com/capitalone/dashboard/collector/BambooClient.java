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

import java.util.Map;
import java.util.Set;

/**
 * Client for fetching job and build information from Bamboo.
 */
public interface BambooClient {

  /**
   * Finds all of the configured jobs for a given instance and returns the set of
   * builds for each job. At a minimum, the number and url of each Build will be
   * populated.
   *
   * @param instanceUrl the URL for the Bamboo instance.
   * @return a summary of every build for each job on the instance.
   */
  Map<BambooJob, Set<Build>> getInstanceJobs(String instanceUrl);

  /**
   * Fetch full populated build information for a build.
   *
   * @param buildUrl    the url of the build.
   * @param instanceUrl the URL for the Bamboo instance.
   * @return a Build instance or null.
   */
  Build getBuildDetails(String buildUrl, String instanceUrl);
}
