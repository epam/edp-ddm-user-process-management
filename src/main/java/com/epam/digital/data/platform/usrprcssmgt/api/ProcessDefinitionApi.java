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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The {@link ProcessDefinitionApi} class represents a facade with operations on  {@link
 * org.camunda.bpm.engine.repository.ProcessDefinition} entity.
 * <p>
 * Provides such methods as:
 * <li>{@link ProcessDefinitionApi#getProcessDefinitionByKey(String)} to get a process definition
 * by key</li>
 * <li>{@link ProcessDefinitionApi#getProcessDefinitions(GetProcessDefinitionsParams)} to get a
 * list of process definitions by params that is defined in {@link GetProcessDefinitionsParams}</li>
 * <li>{@link ProcessDefinitionApi#countProcessDefinitions(GetProcessDefinitionsParams)} to get a
 * count of process definitions by params that is defined in {@link GetProcessDefinitionsParams}</li>
 */
public interface ProcessDefinitionApi {

  /**
   * Method for getting the process definition entity by process definition key.
   *
   * @param key process definition key
   * @return process definition entity
   */
  DdmProcessDefinitionDto getProcessDefinitionByKey(String key);

  /**
   * Method for getting a list of the latest version of  process definitions entities. The list must
   * be sorted by process definition name and in ascending order.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return a list of process definitions.
   */
  List<DdmProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params);

  /**
   * Method for getting thr number of process definitions by parameters.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return an entity that defines the number of process definitions.
   */
  CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params);
}
