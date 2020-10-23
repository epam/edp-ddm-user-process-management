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

import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import java.util.List;

/**
 * Service for {@link ProcessDefinitionResponse} entity. Contains methods for accessing process
 * definitions.
 */
public interface ProcessDefinitionRemoteService {

  /**
   * Method for getting the process definition entity by process definition key.
   *
   * @param key process definition key
   * @return process definition entity
   */
  ProcessDefinitionResponse getProcessDefinitionByKey(String key);

  /**
   * Method for getting a list of the latest version of  process definitions entities. The list must
   * be sorted by process definition name and in ascending order.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return a list of process definitions.
   */
  List<ProcessDefinitionResponse> getProcessDefinitions(GetProcessDefinitionsParams params);

  /**
   * Method for getting thr number of process definitions by parameters.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return an entity that defines the number of process definitions.
   */
  CountResponse countProcessDefinitions(GetProcessDefinitionsParams params);

  /**
   * Starting process instance by process definition key
   *
   * @param key process definition key
   * @return process instance entity
   */
  StartProcessInstanceResponse startProcessInstance(String key);

  /**
   * Starting process instance by process definition key with defining start form data
   *
   * @param key         process definition key
   * @param formDataKey form data storage key
   * @return process instance entity
   */
  StartProcessInstanceResponse startProcessInstance(String key, String formDataKey);
}
