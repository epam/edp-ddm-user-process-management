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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.dto.FormValidationResponseDto;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
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
class ProcessExecutionServiceTest {

  @InjectMocks
  private ProcessExecutionService processExecutionService;
  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);
  @Mock
  private FormDataService formDataService;
  @Mock
  private FormValidationService formValidationService;

  @Captor
  private ArgumentCaptor<StartProcessInstanceDto> startProcessInstanceDtoArgumentCaptor;

  @Test
  void startProcessDefinition() {
    var processDefinitionKey = "processDefinitionKey";
    var processInstanceWithVariablesDto = Mockito.mock(ProcessInstanceWithVariablesDto.class);
    when(processInstanceWithVariablesDto.getId()).thenReturn("processInstanceId");
    when(processInstanceWithVariablesDto.getDefinitionId()).thenReturn("processDefinitionId");
    when(processInstanceWithVariablesDto.isEnded()).thenReturn(true);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey), any()))
        .thenReturn(processInstanceWithVariablesDto);

    var result = processExecutionService.startProcessDefinition(processDefinitionKey);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", "processInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("ended", true);
  }

  @Test
  void startProcessDefinitionWithForm() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = DdmProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var formValidationResult = FormValidationResponseDto.builder().isValid(true).build();
    when(formValidationService.validateForm(startFormKey, formDataDto))
        .thenReturn(formValidationResult);

    var formDataKey = "formDataKey";
    when(formDataService.saveStartFormData(processDefinitionKey, formDataDto))
        .thenReturn(formDataKey);

    var processInstance = Mockito.mock(ProcessInstanceWithVariablesDto.class);
    when(processInstance.getId()).thenReturn("processInstanceId");
    when(processInstance.getDefinitionId()).thenReturn("processDefinitionId");
    when(processInstance.isEnded()).thenReturn(true);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey),
        startProcessInstanceDtoArgumentCaptor.capture())).thenReturn(processInstance);

    var result = processExecutionService.startProcessDefinitionWithForm(processDefinitionKey,
        formDataDto);

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

  @Test
  void startProcessDefinitionWithForm_noFormKey() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var processDefinition = DdmProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .build();
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var ex = assertThrows(StartFormException.class,
        () -> processExecutionService.startProcessDefinitionWithForm(processDefinitionKey,
            formDataDto));

    assertThat(ex.getMessage()).isEqualTo("Start form does not exist!");

    verify(formValidationService, never()).validateForm(anyString(), any(FormDataDto.class));
    verify(formDataService, never()).saveStartFormData(anyString(), any(FormDataDto.class));
    verify(processDefinitionRestClient, never()).startProcessInstanceByKey(anyString(), any());
  }

  @Test
  void startProcessDefinitionWithForm_notValidForm() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = DdmProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var error = ValidationErrorDto.builder()
        .code("code")
        .message("message")
        .traceId("traceId")
        .details(new ErrorsListDto())
        .build();
    var formValidationResult = FormValidationResponseDto.builder()
        .isValid(false)
        .error(error)
        .build();
    when(formValidationService.validateForm(startFormKey, formDataDto))
        .thenReturn(formValidationResult);

    var ex = assertThrows(ValidationException.class,
        () -> processExecutionService.startProcessDefinitionWithForm(processDefinitionKey,
            formDataDto));

    assertThat(ex)
        .hasFieldOrPropertyWithValue("code", error.getCode())
        .hasFieldOrPropertyWithValue("message", error.getMessage())
        .hasFieldOrPropertyWithValue("traceId", error.getTraceId())
        .hasFieldOrPropertyWithValue("details", error.getDetails());

    verify(formDataService, never()).saveStartFormData(anyString(), any(FormDataDto.class));
    verify(processDefinitionRestClient, never()).startProcessInstanceByKey(anyString(), any());
  }
}
