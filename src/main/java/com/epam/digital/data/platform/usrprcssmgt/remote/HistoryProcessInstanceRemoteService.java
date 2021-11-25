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

package com.epam.digital.data.platform.usrprcssmgt.remote;

import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.HistoryUserProcessInstanceResponse;
import java.util.List;

/**
 * Service for {@link HistoryUserProcessInstanceResponse} entity. Contains methods for accessing
 * finished process instances.
 */
public interface HistoryProcessInstanceRemoteService {

  /**
   * Method for getting a list of finished process instance entities. The list must be sorted by end
   * time and in descending order.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list containing all the finished process instances.
   */
  List<HistoryUserProcessInstanceResponse> getHistoryProcessInstances(Pageable page);

  /**
   * Method for getting finished process instance entity by id.
   *
   * @param processInstanceId process instance identifier.
   * @return finished process instance.
   */
  HistoryUserProcessInstanceResponse getHistoryProcessInstanceById(String processInstanceId);
}
