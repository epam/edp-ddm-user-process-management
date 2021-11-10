package com.epam.digital.data.platform.usrprcssmgt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.bpms.client.StartFormRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.util.Collections;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.StubProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;
  @Mock
  private StartFormRestClient startFormRestClient;
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);
  @Spy
  private ProcessDefinitionMapper processDefinitionMapper = Mappers.getMapper(
      ProcessDefinitionMapper.class);

  @Captor
  private ArgumentCaptor<StartProcessInstanceDto> startProcessInstanceDtoArgumentCaptor;

  @Test
  void countProcessDefinitions() {
    var processDefinitionQuery = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .build();
    var expectedCountDto = new CountResultDto(7L);
    when(processDefinitionRestClient.getProcessDefinitionsCount(processDefinitionQuery))
        .thenReturn(expectedCountDto);

    var result = processDefinitionService.countProcessDefinitions(
        new GetProcessDefinitionsParams());

    assertThat(result.getCount()).isEqualTo(7L);
  }

  @Test
  void getProcessDefinitions() {
    var processDefinitionQuery = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue()).build();
    var definition = new StubProcessDefinitionDto();
    definition.setId("id");
    definition.setName("Awesome Definition Name");
    when(processDefinitionRestClient.getProcessDefinitionsByParams(processDefinitionQuery))
        .thenReturn(Collections.singletonList(definition));

    when(startFormRestClient.getStartFormKeyMap(any())).thenReturn(Map.of("id", "testFormKey"));

    var result = processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams());

    var expectedDefinition = UserProcessDefinitionDto.builder()
        .id("id")
        .name("Awesome Definition Name")
        .formKey("testFormKey")
        .build();
    assertThat(result).hasSize(1).contains(expectedDefinition);
  }

  @Test
  void getProcessDefinitionByKey() {
    var processDefinitionId = "id";
    var processDefinitionKey = "processDefinitionKey";

    var definition = new StubProcessDefinitionDto();
    definition.setId(processDefinitionId);
    definition.setName("testName");
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(definition);

    when(startFormRestClient.getStartFormKeyMap(any())).thenReturn(Map.of("id", "testFormKey"));

    var result = processDefinitionService.getProcessDefinitionByKey(processDefinitionKey);

    var expectedDefinition = UserProcessDefinitionDto.builder()
        .id(processDefinitionId)
        .name("testName")
        .formKey("testFormKey")
        .build();
    assertThat(result).isEqualTo(expectedDefinition);
  }

  @Test
  void startProcessInstance() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionKey = "processDefinitionKey";
    var processDefinition = new ProcessDefinitionEntity();
    processDefinition.setKey("processDefinitionKet");
    processDefinition.setId(processDefinitionId);
    var processInstanceId = "processInstanceId";
    var execution = new ExecutionEntity();
    execution.setId(processInstanceId);
    execution.setProcessDefinitionId(processDefinitionId);
    var processInstance = new ProcessInstanceWithVariablesDto(execution);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey), any())).thenReturn(processInstance);

    var result = processDefinitionService.startProcessDefinition(processDefinitionKey);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .build();
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void startProcessDefinition_startFormKey() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionKey = "processDefinitionKey";
    var formDataKey = "formDataKey";

    var processInstanceId = "processInstanceId";
    var execution = new ExecutionEntity();
    execution.setId(processInstanceId);
    execution.setProcessDefinitionId(processDefinitionId);
    var processInstance = new ProcessInstanceWithVariablesDto(execution);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey),
        startProcessInstanceDtoArgumentCaptor.capture())).thenReturn(processInstance);

    var result = processDefinitionService.startProcessDefinition(processDefinitionKey, formDataKey);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
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
