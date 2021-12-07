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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.usrprcssmgt.mapper.BaseMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import java.util.Collections;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionRemoteServiceImplTest {

  @InjectMocks
  private ProcessDefinitionRemoteServiceImpl processDefinitionRemoteService;
  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;
  @Spy
  private BaseMapper baseMapper = Mappers.getMapper(BaseMapper.class);
  @Spy
  private ProcessDefinitionMapper processDefinitionMapper = Mappers.getMapper(
      ProcessDefinitionMapper.class);
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Captor
  private ArgumentCaptor<StartProcessInstanceDto> startProcessInstanceDtoArgumentCaptor;

  @Test
  void countProcessDefinitions() {
    var processDefinitionQuery = DdmProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .build();
    var expectedCountDto = new CountResultDto(7L);
    when(processDefinitionRestClient.getProcessDefinitionsCount(processDefinitionQuery))
        .thenReturn(expectedCountDto);

    var result = processDefinitionRemoteService.countProcessDefinitions(
        new GetProcessDefinitionsParams());

    assertThat(result.getCount()).isEqualTo(7L);
  }

  @Test
  void getProcessDefinitions() {
    var processDefinitionQuery = DdmProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .sortBy(DdmProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue()).build();
    var definition = DdmProcessDefinitionDto.builder()
        .id("id")
        .name("Awesome Definition Name")
        .formKey("testFormKey")
        .build();
    when(processDefinitionRestClient.getProcessDefinitionsByParams(processDefinitionQuery))
        .thenReturn(Collections.singletonList(definition));

    var result = processDefinitionRemoteService.getProcessDefinitions(
        new GetProcessDefinitionsParams());

    assertThat(result).hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("id", "id")
        .hasFieldOrPropertyWithValue("name", "Awesome Definition Name")
        .hasFieldOrPropertyWithValue("formKey", "testFormKey");
  }

  @Test
  void getProcessDefinitionByKey() {
    var processDefinitionId = "id";
    var processDefinitionKey = "processDefinitionKey";

    var definition = DdmProcessDefinitionDto.builder()
        .id(processDefinitionId)
        .key(processDefinitionKey)
        .name("testName")
        .formKey("testFormKey")
        .build();
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(definition);

    var result = processDefinitionRemoteService.getProcessDefinitionByKey(processDefinitionKey);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", processDefinitionId)
        .hasFieldOrPropertyWithValue("key", processDefinitionKey)
        .hasFieldOrPropertyWithValue("name", "testName")
        .hasFieldOrPropertyWithValue("formKey", "testFormKey");
  }

  @Test
  void startProcessInstance() {
    var processDefinitionKey = "processDefinitionKey";

    var processInstance = Mockito.mock(ProcessInstanceWithVariablesDto.class);
    when(processInstance.getId()).thenReturn("processInstanceId");
    when(processInstance.getDefinitionId()).thenReturn("processDefinitionId");
    when(processInstance.isEnded()).thenReturn(true);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey), any()))
        .thenReturn(processInstance);

    var result = processDefinitionRemoteService.startProcessInstance(processDefinitionKey);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .ended(true)
        .build();
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void startProcessInstanceWithForm() {
    var processDefinitionKey = "processDefinitionKey";
    var formDataKey = "formDataKey";

    var processInstance = Mockito.mock(ProcessInstanceWithVariablesDto.class);
    when(processInstance.getId()).thenReturn("processInstanceId");
    when(processInstance.getDefinitionId()).thenReturn("processDefinitionId");
    when(processInstance.isEnded()).thenReturn(true);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey),
        startProcessInstanceDtoArgumentCaptor.capture())).thenReturn(processInstance);

    var result = processDefinitionRemoteService.startProcessInstance(processDefinitionKey, formDataKey);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .ended(true)
        .build();
    assertThat(result).isEqualTo(expectedResponse);

    var startProcessInstanceDto = startProcessInstanceDtoArgumentCaptor.getValue();
    assertThat(startProcessInstanceDto.getVariables()).hasSize(1)
        .containsKey(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME)
        .extractingByKey(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME)
        .extracting(VariableValueDto::getValue)
        .isEqualTo(formDataKey);
  }
}
