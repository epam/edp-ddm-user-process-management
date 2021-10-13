package com.epam.digital.data.platform.usrprcssmgt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.epam.digital.data.platform.usrprcssmgt.controller.config.CustomMockMvcConfigurer;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.StatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessInstanceService;
import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseControllerTest {

  @InjectMocks
  private ProcessDefinitionController processDefinitionController;
  @InjectMocks
  private ProcessInstanceController processInstanceController;

  @Mock
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessInstanceService processInstanceService;

  @Before
  public void setup() {
    RestAssuredMockMvc.standaloneSetup(processDefinitionController, processInstanceController,
        new CustomMockMvcConfigurer());

    // init process definitions controller
    initGetProcessDefinitionsResponse();
    initGetProcessDefinitionByIdResponse();
    initCountProcessDefinitionsResponse();
    initStartProcessInstanceResponse();
    initStartProcessInstanceWithFormResponse();

    // init process instance controller
    initCountProcessInstancesResponse();
    initGetOfficerProcessInstancesResponse();
    initGetCitizenProcessInstancesResponse();
  }

  private void initGetProcessDefinitionsResponse() {
    var processDefinition1 = new UserProcessDefinitionDto();
    processDefinition1.setId("id1");
    processDefinition1.setKey("key1");
    processDefinition1.setName("name1");
    processDefinition1.setSuspended(false);
    processDefinition1.setFormKey("formKey1");
    var processDefinition2 = new UserProcessDefinitionDto();
    processDefinition2.setId("id2");
    processDefinition2.setKey("key2");
    processDefinition2.setName("name2");
    processDefinition2.setSuspended(true);
    processDefinition2.setFormKey("formKey2");

    lenient().when(processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams())).thenReturn(
        Arrays.asList(processDefinition1, processDefinition2));
  }

  private void initGetProcessDefinitionByIdResponse() {
    var processDefinition = new UserProcessDefinitionDto();
    processDefinition.setId("id1");
    processDefinition.setKey("key1");
    processDefinition.setName("name1");
    processDefinition.setSuspended(false);
    processDefinition.setFormKey("formKey1");

    lenient().when(processDefinitionService.getProcessDefinitionByKey("processDefinitionKey"))
        .thenReturn(processDefinition);
  }

  private void initCountProcessDefinitionsResponse() {
    lenient().when(processDefinitionService.countProcessDefinitions(new GetProcessDefinitionsParams()))
        .thenReturn(new CountResultDto(2L));
  }

  private void initStartProcessInstanceResponse() {
    lenient().when(processDefinitionService.startProcessDefinition("processDefinitionKey"))
        .thenReturn(StartProcessInstanceResponse.builder().id("processInstanceId")
            .processDefinitionId("processDefinitionId").ended(true).build());
  }

  private void initStartProcessInstanceWithFormResponse() {
    lenient().when(processDefinitionService.startProcessDefinitionWithForm(eq("processDefinitionKey"), any()))
        .thenReturn(StartProcessInstanceResponse.builder().id("processInstanceId")
            .processDefinitionId("processDefinitionId").ended(false).build());
  }

  private void initCountProcessInstancesResponse() {
    lenient().when(processInstanceService.countProcessInstances()).thenReturn(new CountResultDto(3L));
  }

  private void initGetOfficerProcessInstancesResponse() {
    lenient().when(processInstanceService.getOfficerProcessInstances(new Pageable())).thenReturn(ImmutableList.of(
        GetProcessInstanceResponse.builder()
            .id("id1").processDefinitionName("name1")
            .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
            .status(StatusModel.builder()
                .code(ProcessInstanceStatus.SUSPENDED.name())
                .build()).build(),
        GetProcessInstanceResponse.builder()
            .id("id2").processDefinitionName("name2")
            .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
            .status(StatusModel.builder()
                .code(ProcessInstanceStatus.PENDING.name())
                .build()).build()));
  }

  private void initGetCitizenProcessInstancesResponse() {
    lenient().when(processInstanceService.getCitizenProcessInstances(new Pageable())).thenReturn(ImmutableList.of(
        GetProcessInstanceResponse.builder()
            .id("id3").processDefinitionName("name3")
            .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
            .status(StatusModel.builder()
                .code(ProcessInstanceStatus.CITIZEN_SUSPENDED.name())
                .build()).build(),
        GetProcessInstanceResponse.builder()
            .id("id4").processDefinitionName("name4")
            .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
            .status(StatusModel.builder()
                .code(ProcessInstanceStatus.CITIZEN_PENDING.name())
                .build()).build()));
  }
}
