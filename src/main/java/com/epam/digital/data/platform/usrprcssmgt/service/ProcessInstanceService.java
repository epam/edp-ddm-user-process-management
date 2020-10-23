/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.starter.security.SystemRole;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.ProcessInstanceRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A service that contains methods for working with a history user process instances.
 * <p>
 * Implements such business functions:
 * <li>{@link ProcessInstanceService#countProcessInstances() getting count of running process
 * instances}</li>
 * <li>{@link ProcessInstanceService#getOfficerProcessInstances(Pageable) Getting not completed
 * officer processInstances}</li>
 * <li>{@link ProcessInstanceService#getCitizenProcessInstances(Pageable) Getting not completed
 * citizen processInstances}</li>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService {

  private final ProcessInstanceRemoteService processInstanceRemoteService;

  /**
   * Getting count of unfinished user process instances
   *
   * @return dto with count of unfinished user process instances
   */
  public CountResponse countProcessInstances() {
    log.info("Getting count of unfinished process instances");

    var result = processInstanceRemoteService.countProcessInstances();

    log.info("Count of unfinished process instances is found - {}", result.getCount());
    return result;
  }

  /**
   * Getting list of unfinished user process instances for officer
   *
   * @return list with entities of unfinished user process instances
   */
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page) {
    log.info("Getting unfinished officer process instances. Parameters: {}", page);

    var result = processInstanceRemoteService.getProcessInstances(page, SystemRole.OFFICER);

    log.info("Found {} unfinished officer process instances", result.size());
    return result;
  }

  /**
   * Getting list of unfinished user process instances for citizen
   *
   * @return list with entities of unfinished user process instances
   */
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page) {
    log.info("Getting unfinished citizen process instances. Parameters: {}", page);

    var result = processInstanceRemoteService.getProcessInstances(page, SystemRole.CITIZEN);

    log.info("Found {} unfinished citizen process instances", result.size());
    return result;
  }
}
