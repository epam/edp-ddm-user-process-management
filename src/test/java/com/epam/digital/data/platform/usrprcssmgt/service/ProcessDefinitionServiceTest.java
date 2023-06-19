/*
 * Copyright 2023 EPAM Systems.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.formprovider.dto.FormDataValidationDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.dto.FormValidationResponseDto;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.usrprcssmgt.config.properties.BpGroupConfigurationProperties;
import com.epam.digital.data.platform.usrprcssmgt.config.properties.BpGroupConfigurationProperties.GroupedProcessDefinition;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.ProcessDefinitionGroup;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GroupedProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.ProcessDefinitionRemoteService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessDefinitionRemoteService processDefinitionRemoteService;
  @Mock
  private FormDataStorageService formDataStorageService;
  @Mock
  private FormValidationService formValidationService;
  @Mock
  private BpGroupConfigurationProperties bpGroupConfigurationProperties;

  @Test
  void startProcessInstance() {
    var processDefinitionKey = "processDefinitionKey";
    var expectedResponse = StartProcessInstanceResponse.builder()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .ended(true)
        .build();


    var formDataKey = "formDataKey";
    var authentication = mock(Authentication.class);
    when(authentication.getCredentials()).thenReturn("token");
    when(formDataStorageService.putStartFormData(eq(processDefinitionKey), anyString(), any()))
            .thenReturn(formDataKey);
    when(processDefinitionRemoteService.startProcessInstance(processDefinitionKey, formDataKey))
            .thenReturn(expectedResponse);
    var result = processDefinitionService.startProcessInstance(processDefinitionKey, authentication);

    assertThat(result).isSameAs(expectedResponse);
    verify(formDataStorageService, never()).delete(any());
  }

  @Test
  void startProcessDefinitionWithForm() {
    var formDataDto = mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = ProcessDefinitionResponse.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionRemoteService.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var formValidationResult = FormValidationResponseDto.builder().isValid(true).build();
    when(formValidationService.validateForm(eq(startFormKey), any()))
        .thenReturn(formValidationResult);

    var formDataKey = "formDataKey";
    when(formDataStorageService.putStartFormData(eq(processDefinitionKey), anyString(), eq(formDataDto)))
        .thenReturn(formDataKey);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .ended(true)
        .build();
    when(processDefinitionRemoteService.startProcessInstance(processDefinitionKey,
        formDataKey)).thenReturn(expectedResponse);

    var authentication = mock(Authentication.class);
    when(authentication.getCredentials()).thenReturn("token");

    var result = processDefinitionService.startProcessInstanceWithForm(processDefinitionKey,
        formDataDto, authentication);

    assertThat(result).isEqualTo(expectedResponse);
    verify(formDataStorageService, never()).delete(any());
  }

  @Test
  void startProcessDefinitionWithForm_noFormKey() {
    var formDataDto = mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var processDefinition = ProcessDefinitionResponse.builder()
        .key(processDefinitionKey)
        .build();
    when(processDefinitionRemoteService.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var authentication = mock(Authentication.class);

    var ex = assertThrows(StartFormException.class,
        () -> processDefinitionService.startProcessInstanceWithForm(processDefinitionKey,
            formDataDto, authentication));

    assertThat(ex.getMessage()).isEqualTo("Start form does not exist!");

    verify(formValidationService, never()).validateForm(anyString(), any(FormDataValidationDto.class));
    verify(formDataStorageService, never()).putFormData(anyString(), anyString(), any(FormDataDto.class));
    verify(formDataStorageService, never()).delete(any());
    verify(processDefinitionRemoteService, never()).startProcessInstance(anyString(), anyString());
  }

  @Test
  void startProcessDefinitionWithForm_notValidForm() {
    var formDataDto = mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = ProcessDefinitionResponse.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionRemoteService.getProcessDefinitionByKey(processDefinitionKey))
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
    when(formValidationService.validateForm(eq(startFormKey), any()))
        .thenReturn(formValidationResult);

    var authentication = mock(Authentication.class);

    var ex = assertThrows(ValidationException.class,
        () -> processDefinitionService.startProcessInstanceWithForm(processDefinitionKey,
            formDataDto, authentication));

    assertThat(ex)
        .hasFieldOrPropertyWithValue("code", error.getCode())
        .hasFieldOrPropertyWithValue("message", error.getMessage())
        .hasFieldOrPropertyWithValue("traceId", error.getTraceId())
        .hasFieldOrPropertyWithValue("details", error.getDetails());

    verify(formDataStorageService, never()).putStartFormData(anyString(), anyString(), any(FormDataDto.class));
    verify(formDataStorageService, never()).delete(any());
    verify(processDefinitionRemoteService, never()).startProcessInstance(anyString(), anyString());
  }

  @Test
  void shouldDeleteStartFormDataWhenErrorOccursWhenStartingProcessDefinitionWithForm() {
    var formDataDto = mock(FormDataDto.class);
    var authentication = mock(Authentication.class);
    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = ProcessDefinitionResponse.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    var formDataKey = "formDataKey";
    var formValidationResult = FormValidationResponseDto.builder().isValid(true).build();

    when(processDefinitionRemoteService.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);
    when(formValidationService.validateForm(eq(startFormKey), any()))
        .thenReturn(formValidationResult);
    when(authentication.getCredentials()).thenReturn("token");
    when(formDataStorageService.putStartFormData(eq(processDefinitionKey), anyString(), eq(formDataDto)))
        .thenReturn(formDataKey);
    when(processDefinitionRemoteService.startProcessInstance(processDefinitionKey,
        formDataKey)).thenThrow(ValidationException.class);

    assertThrows(ValidationException.class,
        () -> processDefinitionService.startProcessInstanceWithForm(processDefinitionKey,
            formDataDto, authentication));
    verify(processDefinitionRemoteService).getProcessDefinitionByKey(processDefinitionKey);
    var formValidationDto = FormDataValidationDto.builder().data(formDataDto.getData()).build();
    verify(formValidationService).validateForm(startFormKey, formValidationDto);
    verify(formDataStorageService).putStartFormData(eq(processDefinitionKey), anyString(), any(FormDataDto.class));
    verify(processDefinitionRemoteService).startProcessInstance(processDefinitionKey, formDataKey);
    verify(formDataStorageService).delete(Set.of(formDataKey));
  }

  @Test
  void shouldDeleteAccessTokenFormDataWhenErrorOccursWhenStartingProcessDefinition() {
    var processDefinitionKey = "processDefinitionKey";
    var formDataKey = "formDataKey";
    var authentication = mock(Authentication.class);

    when(authentication.getCredentials()).thenReturn("token");
    when(formDataStorageService.putStartFormData(eq(processDefinitionKey), anyString(), any()))
        .thenReturn(formDataKey);
    when(processDefinitionRemoteService.startProcessInstance(processDefinitionKey,
        formDataKey)).thenThrow(ValidationException.class);

    assertThrows(ValidationException.class,
        () -> processDefinitionService.startProcessInstance(processDefinitionKey, authentication));
    verify(formDataStorageService).putStartFormData(eq(processDefinitionKey), anyString(), any(FormDataDto.class));
    verify(processDefinitionRemoteService).startProcessInstance(processDefinitionKey, formDataKey);
    verify(formDataStorageService).delete(Set.of(formDataKey));
  }

  @Test
  void shouldObtainGroupedProcessDefinitions() {
    var params = new GetProcessDefinitionsParams();
    var groupName = "test";
    var processDefinition = ProcessDefinitionResponse.builder().key("123").name("name1").build();
    var processDefinition2 = ProcessDefinitionResponse.builder().key("234").name("name2").build();
    var processDefinition3 = ProcessDefinitionResponse.builder().key("345").name("name3").build();
    var processDefinitionResponses = List.of(processDefinition, processDefinition2, processDefinition3);
    var groups = new ArrayList<GroupedProcessDefinition>();
    var groupedDefinitionKeys = List.of("123");
    var groupedProcessDefinition = new GroupedProcessDefinition();
    groupedProcessDefinition.setProcessDefinitions(groupedDefinitionKeys);
    groupedProcessDefinition.setName(groupName);
    groups.add(groupedProcessDefinition);
    var ungrouped = List.of("234");
    var processDefinitionGroup = ProcessDefinitionGroup.builder()
        .name(groupName)
        .processDefinitions(List.of(processDefinition))
        .build();
    var expectedResponse = GroupedProcessDefinitionResponse.builder()
        .groups(List.of(processDefinitionGroup))
        .ungrouped(List.of(processDefinition2, processDefinition3))
        .build();

    when(bpGroupConfigurationProperties.getGroups()).thenReturn(groups);
    when(bpGroupConfigurationProperties.getUngrouped()).thenReturn(ungrouped);
    when(processDefinitionRemoteService.getProcessDefinitions(params)).thenReturn(processDefinitionResponses);

    var result = processDefinitionService.getGroupedProcessDefinitions(params);

    assertThat(result).isEqualTo(expectedResponse);
    verify(processDefinitionRemoteService).getProcessDefinitions(params);
  }
}
