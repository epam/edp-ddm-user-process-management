package com.epam.digital.data.platform.usrprcssmgt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.dto.FormValidationResponseDto;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {

  @InjectMocks
  private ProcessExecutionService processExecutionService;
  @Mock
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private FormDataService formDataService;
  @Mock
  private FormValidationService formValidationService;

  @Test
  void startProcessDefinition() {
    var processDefinitionId = "processDefinitionId";
    var startProcessInstanceResponse = Mockito.mock(StartProcessInstanceResponse.class);
    when(processDefinitionService.startProcessDefinition(processDefinitionId))
        .thenReturn(startProcessInstanceResponse);

    var result = processExecutionService.startProcessDefinition(processDefinitionId);

    assertThat(result).isSameAs(startProcessInstanceResponse);
  }

  @Test
  void startProcessDefinitionWithForm() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = UserProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionService.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var formValidationResult = FormValidationResponseDto.builder().isValid(true).build();
    when(formValidationService.validateForm(startFormKey, formDataDto))
        .thenReturn(formValidationResult);

    var formDataKey = "formDataKey";
    when(formDataService.saveStartFormData(processDefinitionKey, formDataDto))
        .thenReturn(formDataKey);

    var startProcessInstanceResponse = Mockito.mock(StartProcessInstanceResponse.class);
    when(processDefinitionService.startProcessDefinition(processDefinitionKey, formDataKey))
        .thenReturn(startProcessInstanceResponse);

    var result = processExecutionService.startProcessDefinitionWithForm(processDefinitionKey,
        formDataDto);

    assertThat(result).isSameAs(startProcessInstanceResponse);
  }

  @Test
  void startProcessDefinitionWithForm_noFormKey() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var processDefinition = UserProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .build();
    when(processDefinitionService.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(processDefinition);

    var ex = assertThrows(StartFormException.class,
        () -> processExecutionService.startProcessDefinitionWithForm(processDefinitionKey,
            formDataDto));

    assertThat(ex.getMessage()).isEqualTo("Start form does not exist!");

    verify(formValidationService, never()).validateForm(anyString(), any(FormDataDto.class));
    verify(formDataService, never()).saveStartFormData(anyString(), any(FormDataDto.class));
    verify(processDefinitionService, never()).startProcessDefinition(anyString(), anyString());
  }

  @Test
  void startProcessDefinitionWithForm_notValidForm() {
    var formDataDto = Mockito.mock(FormDataDto.class);

    var processDefinitionKey = "processDefinitionKey";
    var startFormKey = "startFormKey";
    var processDefinition = UserProcessDefinitionDto.builder()
        .key(processDefinitionKey)
        .formKey(startFormKey)
        .build();
    when(processDefinitionService.getProcessDefinitionByKey(processDefinitionKey))
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
    verify(processDefinitionService, never()).startProcessDefinition(anyString(), anyString());
  }
}
