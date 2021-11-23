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

package com.epam.digital.data.platform.usrprcssmgt.api;

import com.epam.digital.data.platform.usrprcssmgt.model.HistoryUserProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The {@link HistoryProcessInstanceApi} class represents a facade with operations on  {@link
 * HistoryUserProcessInstance} entity. It contains methods for working with a finished process
 * instance.
 * <p>
 * Provides such methods as:
 * <li>{@link HistoryProcessInstanceApi#getHistoryProcessInstances(Pageable)} to get a pageable
 * list of finished process instances</li>
 * <li>{@link HistoryProcessInstanceApi#getHistoryProcessInstanceById(String)} to get a finished
 * process instance by the specified identifier</li>
 * <li>{@link HistoryProcessInstanceApi#getCountProcessInstances()} to get a finished process
 * instance count</li>
 */
public interface HistoryProcessInstanceApi {

  /**
   * Method for getting a list of finished process instance entities. The list must be sorted by end
   * time and in descending order.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list containing all the finished process instances.
   */
  List<HistoryUserProcessInstance> getHistoryProcessInstances(Pageable page);

  /**
   * Method for getting finished process instance entity by id.
   *
   * @param processInstanceId process instance identifier.
   * @return finished process instance.
   */
  HistoryUserProcessInstance getHistoryProcessInstanceById(String processInstanceId);

  /**
   * Method for getting the number of root finished process instances
   *
   * @return an entity that defines the number of finished process instances.
   */
  CountResultDto getCountProcessInstances();
}
