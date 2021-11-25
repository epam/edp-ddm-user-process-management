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

import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.HistoryUserProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.HistoryProcessInstanceRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A service that contains methods for working with a history user process instances.
 * <p>
 * Implements such business functions:
 * <li>{@link HistoryProcessInstanceService#getHistoryProcessInstances(Pageable) Getting completed
 * user processInstances}</li>
 * <li>{@link HistoryProcessInstanceService#getHistoryProcessInstanceById(String) Getting completed
 * user processInstance by id}</li>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryProcessInstanceService {

  private final HistoryProcessInstanceRemoteService historyProcessInstanceRemoteService;

  /**
   * Getting list of completed user process instances
   *
   * @param page object that contains paging and sorting parameters
   * @return list of completed user process instances
   */
  public List<HistoryUserProcessInstanceResponse> getHistoryProcessInstances(Pageable page) {
    log.info("Getting finished process instances. Parameters: {}", page);

    var result = historyProcessInstanceRemoteService.getHistoryProcessInstances(page);

    log.info("{} process instances are found", result.size());
    return result;
  }

  /**
   * Getting completed user process instance entity by id
   *
   * @param processInstanceId process instance id to select
   * @return completed user process instance entity by id
   */
  public HistoryUserProcessInstanceResponse getHistoryProcessInstanceById(
      String processInstanceId) {
    log.info("Get finished process instance by id {}", processInstanceId);

    var result = historyProcessInstanceRemoteService.getHistoryProcessInstanceById(
        processInstanceId);

    log.info("Finished process instance by id {} found", processInstanceId);
    return result;
  }
}
