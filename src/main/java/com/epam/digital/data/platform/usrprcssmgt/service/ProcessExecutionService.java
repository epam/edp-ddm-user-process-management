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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessExecutionApi;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessExecutionApi}. Contains logic about starting business
 * process instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessExecutionService implements ProcessExecutionApi {

  private final ProcessDefinitionRestClient processDefinitionRestClient;
  private final ProcessInstanceMapper processInstanceMapper;
  private final FormDataService formDataService;
  private final FormValidationService formValidationService;

  @Override
  public StartProcessInstanceResponse startProcessDefinition(String key) {
    log.info("Starting process instance for definition with key {}", key);

    var result = startProcessInstance(key, new StartProcessInstanceDto());

    log.info("Process instance for process definition {} started. Process instance id {}", key,
        result.getId());
    return result;
  }

  @Override
  public StartProcessInstanceResponse startProcessDefinitionWithForm(String key,
      FormDataDto formDataDto) {
    log.info("Starting process instance with start form for definition with key {}", key);

    var processDefinition = processDefinitionRestClient.getProcessDefinitionByKey(key);
    var startFormKey = getStartFormKey(processDefinition);
    log.trace("Found process definition with key - {} and formKey - {}. Id - {}",
        key, startFormKey, processDefinition.getId());

    validateFormData(startFormKey, formDataDto);
    log.trace("Process definition form data is valid. Id - {}", processDefinition.getId());

    var formDataKey = formDataService.saveStartFormData(key, formDataDto);
    log.trace("Process definition form data was saved. Id - {}", processDefinition.getId());

    var result = startProcessDefinition(key, formDataKey);

    log.info("Starting process instance of process definition {} with id - {} finished. "
        + "Process instance id {}", key, processDefinition.getId(), result.getId());
    return result;
  }

  private String getStartFormKey(DdmProcessDefinitionDto processDefinition) {
    var startFormKey = processDefinition.getFormKey();
    if (Objects.nonNull(startFormKey)) {
      return startFormKey;
    }
    log.warn("Start form key for process definition {} not found!", processDefinition.getKey());
    throw new StartFormException("Start form does not exist!");
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    log.debug("Start validation of start formData");
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      log.warn("Start form data did not pass validation, form key: {}", formId);
      throw new ValidationException(formValidationResponseDto.getError());
    }
    log.debug("FormData passed the validation");
  }

  private StartProcessInstanceResponse startProcessInstance(String key,
      StartProcessInstanceDto startProcessInstanceDto) {
    var processInstanceDto = processDefinitionRestClient.startProcessInstanceByKey(key,
        startProcessInstanceDto);
    log.trace("Process instance started. Process instanceId - {}", processInstanceDto.getId());

    return processInstanceMapper.toStartProcessInstanceResponse(processInstanceDto);
  }

  private StartProcessInstanceResponse startProcessDefinition(String key, String formDataKey) {
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(formDataKey);
    var variables =
        Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, variableValueDto);

    var startProcessInstanceDto = new StartProcessInstanceDto();
    startProcessInstanceDto.setVariables(variables);

    log.trace("Starting instance of process definition. Key - {}", key);
    return startProcessInstance(key, startProcessInstanceDto);
  }
}
