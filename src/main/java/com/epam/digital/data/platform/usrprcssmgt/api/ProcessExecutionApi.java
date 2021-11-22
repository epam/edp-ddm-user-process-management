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

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import org.springframework.security.core.Authentication;

/**
 * The {@link ProcessExecutionApi} class represents a service with operations of a business process
 * starting
 * <p>
 * Provides such methods as:
 * <li>{@link ProcessExecutionApi#startProcessDefinition(String)} to start a process instance of a
 * process definition by id </li>
 * <li>{@link ProcessExecutionApi#startProcessDefinitionWithForm(String, FormDataDto,
 * Authentication)} to start a
 * process instance of a process definition by id with start form</li>
 */
public interface ProcessExecutionApi {

  /**
   * Method for running process instance by process definition id, returns started process instance
   * entity.
   *
   * @param key process definition key
   * @return an entity that defines the started process instance
   */
  StartProcessInstanceResponse startProcessDefinition(String key);

  /**
   * Method for running process instance by process definition id with start form, returns started
   * process instance entity.
   *
   * @param key            process definition key
   * @param formDataDto    start from data
   * @param authentication object with authentication data
   * @return an entity that defines the started process instance
   * @throws StartFormException  if there's not defined start form key for process definition
   * @throws ValidationException if form data hasn't passed the validation
   */
  StartProcessInstanceResponse startProcessDefinitionWithForm(String key, FormDataDto formDataDto,
      Authentication authentication);
}
