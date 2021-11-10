package com.epam.digital.data.platform.usrprcssmgt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.epam.digital.data.platform.usrprcssmgt.api.HistoryProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessDefinitionApi;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessExecutionApi;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.controller.config.CustomMockMvcConfigurer;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryStatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.StatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

  @InjectMocks
  private ProcessDefinitionController processDefinitionController;
  @InjectMocks
  private ProcessInstanceController processInstanceController;
  @InjectMocks
  private HistoryProcessInstanceController historyProcessInstanceController;

  @Mock
  private ProcessDefinitionApi processDefinitionApi;
  @Mock
  private ProcessExecutionApi processExecutionApi;
  @Mock
  private ProcessInstanceApi processInstanceApi;
  @Mock
  private HistoryProcessInstanceApi historyProcessInstanceApi;

  @BeforeEach
  public void setup() {
    RestAssuredMockMvc.standaloneSetup(processDefinitionController, processInstanceController,
        historyProcessInstanceController, new CustomMockMvcConfigurer());

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

    // init history process instance controller
    initCountHistoryProcessInstancesResponse();
    initGetHistoryProcessInstanceByIdResponse();
    initGetHistoryProcessInstancesResponse();
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

    lenient()
        .when(processDefinitionApi.getProcessDefinitions(new GetProcessDefinitionsParams()))
        .thenReturn(List.of(processDefinition1, processDefinition2));
  }

  private void initGetProcessDefinitionByIdResponse() {
    var processDefinition = new UserProcessDefinitionDto();
    processDefinition.setId("id1");
    processDefinition.setKey("key1");
    processDefinition.setName("name1");
    processDefinition.setSuspended(false);
    processDefinition.setFormKey("formKey1");

    lenient()
        .when(processDefinitionApi.getProcessDefinitionByKey("processDefinitionKey"))
        .thenReturn(processDefinition);
  }

  private void initCountProcessDefinitionsResponse() {
    lenient()
        .when(processDefinitionApi.countProcessDefinitions(new GetProcessDefinitionsParams()))
        .thenReturn(new CountResultDto(2L));
  }

  private void initStartProcessInstanceResponse() {
    lenient()
        .when(processExecutionApi.startProcessDefinition("processDefinitionKey"))
        .thenReturn(StartProcessInstanceResponse.builder().id("processInstanceId")
            .processDefinitionId("processDefinitionId").ended(true).build());
  }

  private void initStartProcessInstanceWithFormResponse() {
    var expectedResponse = StartProcessInstanceResponse.builder()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .ended(false)
        .build();
    lenient()
        .when(processExecutionApi.startProcessDefinitionWithForm(eq("processDefinitionKey"),
            any()))
        .thenReturn(expectedResponse);
  }

  private void initCountProcessInstancesResponse() {
    lenient()
        .when(processInstanceApi.countProcessInstances())
        .thenReturn(new CountResultDto(3L));
  }

  private void initGetOfficerProcessInstancesResponse() {
    var processInstance1 = GetProcessInstanceResponse.builder()
        .id("id1")
        .processDefinitionName("name1")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .status(StatusModel.builder()
            .code(ProcessInstanceStatus.SUSPENDED.name())
            .build())
        .build();
    var processInstance2 = GetProcessInstanceResponse.builder()
        .id("id2")
        .processDefinitionName("name2")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
        .status(StatusModel.builder()
            .code(ProcessInstanceStatus.PENDING.name())
            .build())
        .build();
    lenient()
        .when(processInstanceApi.getOfficerProcessInstances(new Pageable()))
        .thenReturn(List.of(processInstance1, processInstance2));
  }

  private void initGetCitizenProcessInstancesResponse() {
    var processInstance1 = GetProcessInstanceResponse.builder()
        .id("id3")
        .processDefinitionName("name3")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .status(StatusModel.builder()
            .code(ProcessInstanceStatus.CITIZEN_SUSPENDED.name())
            .build())
        .build();
    var processInstance2 = GetProcessInstanceResponse.builder()
        .id("id4")
        .processDefinitionName("name4")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
        .status(StatusModel.builder()
            .code(ProcessInstanceStatus.CITIZEN_PENDING.name())
            .build())
        .build();
    lenient()
        .when(processInstanceApi.getCitizenProcessInstances(new Pageable()))
        .thenReturn(List.of(processInstance1, processInstance2));
  }

  private void initCountHistoryProcessInstancesResponse() {
    lenient()
        .when(historyProcessInstanceApi.getCountProcessInstances())
        .thenReturn(new CountResultDto(2L));
  }

  private void initGetHistoryProcessInstanceByIdResponse() {
    var historyProcessInstance = HistoryProcessInstance.builder()
        .id("historyProcessInstanceId1")
        .processDefinitionId("processDefinitionId1")
        .processDefinitionName("name3")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .endTime(LocalDateTime.of(2020, 12, 1, 13, 0))
        .status(HistoryStatusModel.builder()
            .code("ENDED")
            .build())
        .excerptId("excerptId1")
        .build();

    lenient()
        .when(historyProcessInstanceApi
            .getHistoryProcessInstanceById("historyProcessInstanceId1"))
        .thenReturn(historyProcessInstance);
  }

  private void initGetHistoryProcessInstancesResponse() {
    var historyProcessInstance1 = HistoryProcessInstance.builder()
        .id("historyProcessInstanceId1")
        .processDefinitionId("processDefinitionId1")
        .processDefinitionName("name3")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .endTime(LocalDateTime.of(2020, 12, 1, 13, 0))
        .status(HistoryStatusModel.builder()
            .code("ENDED")
            .build())
        .excerptId("excerptId1")
        .build();
    var historyProcessInstance2 = HistoryProcessInstance.builder()
        .id("historyProcessInstanceId2")
        .processDefinitionId("processDefinitionId2")
        .processDefinitionName("name4")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .endTime(LocalDateTime.of(2020, 12, 1, 13, 0))
        .status(HistoryStatusModel.builder()
            .code("ENDED")
            .build())
        .excerptId(null)
        .build();

    lenient()
        .when(historyProcessInstanceApi
            .getHistoryProcessInstances(new Pageable()))
        .thenReturn(List.of(historyProcessInstance1, historyProcessInstance2));
  }
}
