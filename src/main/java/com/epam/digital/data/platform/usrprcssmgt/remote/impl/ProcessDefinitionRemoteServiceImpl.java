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

package com.epam.digital.data.platform.usrprcssmgt.remote.impl;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.usrprcssmgt.mapper.BaseMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.ProcessDefinitionRemoteService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionRemoteServiceImpl implements ProcessDefinitionRemoteService {

  private final ProcessDefinitionRestClient processDefinitionRestClient;

  private final BaseMapper baseMapper;
  private final ProcessDefinitionMapper processDefinitionMapper;
  private final ProcessInstanceMapper processInstanceMapper;

  @Override
  public ProcessDefinitionResponse getProcessDefinitionByKey(String key) {
    log.debug("Selecting process definition by key {} from bpms.", key);

    var dto = processDefinitionRestClient.getProcessDefinitionByKey(key);

    log.debug("Process definition with key {} is found. {}", key, dto);
    return processDefinitionMapper.toProcessDefinitionResponse(dto);
  }

  @Override
  public List<ProcessDefinitionResponse> getProcessDefinitions(GetProcessDefinitionsParams params) {
    log.debug("Selecting list of process definitions form bpms. Params: {}", params);

    var queryDto = DdmProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .sortBy(DdmProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue())
        .build();
    var dtos = processDefinitionRestClient.getProcessDefinitionsByParams(queryDto);

    log.debug("Found process definitions - {}", dtos);
    return processDefinitionMapper.toProcessDefinitionResponseList(dtos);
  }

  @Override
  public CountResponse countProcessDefinitions(GetProcessDefinitionsParams params) {
    log.debug("Selecting count of process definitions form bpms. Params: {}", params);

    var queryDto = DdmProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .build();
    var dto = processDefinitionRestClient.getProcessDefinitionsCount(queryDto);

    return baseMapper.toCountResponse(dto);
  }

  @Override
  public StartProcessInstanceResponse startProcessInstance(String key) {
    log.debug("Starting bpms process instance for definition with key {}", key);

    var result = startProcessInstance(key, new StartProcessInstanceDto());

    log.debug("Process instance for process definition {} started. {}", key, result);
    return result;
  }

  @Override
  public StartProcessInstanceResponse startProcessInstance(String key, String formDataKey) {
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(formDataKey);
    var variables =
        Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, variableValueDto);

    var startProcessInstanceDto = new StartProcessInstanceDto();
    startProcessInstanceDto.setVariables(variables);

    log.trace("Starting instance of process definition. Key - {}", key);
    return startProcessInstance(key, startProcessInstanceDto);
  }

  private StartProcessInstanceResponse startProcessInstance(String key,
      StartProcessInstanceDto startProcessInstanceDto) {
    var processInstanceDto = processDefinitionRestClient.startProcessInstanceByKey(key,
        startProcessInstanceDto);
    log.trace("Process instance started. Process instanceId - {}", processInstanceDto.getId());

    return processInstanceMapper.toStartProcessInstanceResponse(processInstanceDto);
  }
}
