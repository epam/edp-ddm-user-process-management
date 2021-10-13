package com.epam.digital.data.platform.usrprcssmgt.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.bpms.client.StartFormRestClient;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.validation.dto.FormValidationResponseDto;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import com.epam.digital.data.platform.usrprcssmgt.util.CephKeyProvider;
import java.util.Collections;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.StubProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;
  @Mock
  private StartFormRestClient startFormRestClient;
  @Mock
  private CephKeyProvider cephKeyProvider;
  @Mock
  private FormDataCephService formDataCephService;
  @Mock
  private FormValidationService formValidationService;
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(ProcessInstanceMapper.class);
  @Spy
  private ProcessDefinitionMapper processDefinitionMapper = Mappers.getMapper(ProcessDefinitionMapper.class);

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;

  @Test
  public void countProcessDefinitions() {
    var expectedCountDto = new CountResultDto(7L);
    when(processDefinitionRestClient.getProcessDefinitionsCount(
        ProcessDefinitionQueryDto.builder().latestVersion(true).
            active(true).suspended(false).build())).thenReturn(expectedCountDto);

    var result = processDefinitionService.countProcessDefinitions(new GetProcessDefinitionsParams());

    assertThat(result.getCount(), is(7L));
  }

  @Test
  public void getProcessDefinitions() {
    var expectedDefinition =
        UserProcessDefinitionDto.builder().id("id")
            .name("Awesome Definition Name")
            .formKey("testFormKey")
            .build();
    var definition = new StubProcessDefinitionDto();
    definition.setId("id");
    definition.setName("Awesome Definition Name");
    when(processDefinitionRestClient.getProcessDefinitionsByParams(
        ProcessDefinitionQueryDto.builder().latestVersion(true)
            .active(true)
            .suspended(false)
            .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
            .sortOrder(SortOrder.ASC.stringValue()).build()))
        .thenReturn(Collections.singletonList(definition));
    when(startFormRestClient.getStartFormKeyMap(any())).thenReturn(Map.of("id","testFormKey"));

    var result = processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams());

    assertThat(result, hasSize(1));
    assertThat(result.get(0), is(expectedDefinition));
  }

  @Test
  public void getProcessDefinitionByKey() {
    var processDefinitionId = "id";
    var processDefinitionKey = "processDefinitionKey";
    var expectedDefinition = UserProcessDefinitionDto.builder()
        .id(processDefinitionId)
        .name("testName")
        .formKey("testFormKey")
        .build();
    var definition = new StubProcessDefinitionDto();
    definition.setId(processDefinitionId);
    definition.setName("testName");
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey)).thenReturn(definition);
    when(startFormRestClient.getStartFormKeyMap(any())).thenReturn(Map.of("id","testFormKey"));

    var result = processDefinitionService.getProcessDefinitionByKey(processDefinitionKey);

    assertThat(result, is(expectedDefinition));
  }

  @Test
  public void startProcessInstance() {
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

    assertThat(result, is(StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .build()));
  }

  @Test
  public void startProcessInstanceWithForm() {
    var processDefinitionId = "processDefinitionId";
    var processInstanceId = "processInstanceId";
    var processDefinitionKey = "processDefinitionKey";
    var cephKey = "cephKey";
    var formValidationResp = FormValidationResponseDto.builder().isValid(true).build();

    var execution = new ExecutionEntity();
    execution.setId(processInstanceId);
    execution.setProcessDefinitionId(processDefinitionId);

    var formDto = new FormDto();
    formDto.setKey("formKey");

    var processDefinitionEntity = new ProcessDefinitionEntity();
    processDefinitionEntity.setKey(processDefinitionKey);

    var processInstance = new ProcessInstanceWithVariablesDto(execution);

    when(processDefinitionRestClient.getStartFormByKey(processDefinitionKey)).thenReturn(formDto);
    when(cephKeyProvider.generateStartFormKey(eq(processDefinitionKey), any())).thenReturn(cephKey);
    when(processDefinitionRestClient.startProcessInstanceByKey(eq(processDefinitionKey), any())).thenReturn(processInstance);
    doNothing().when(formDataCephService).putFormData(eq(cephKey), any());
    when(formValidationService.validateForm(eq("formKey"), any())).thenReturn(formValidationResp);

    var result = processDefinitionService.startProcessDefinitionWithForm(processDefinitionKey, new FormDataDto());

    assertThat(result, is(StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .build()));
  }
}
