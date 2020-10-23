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

package com.epam.digital.data.platform.usrprcssmgt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.epam.digital.data.platform.usrprcssmgt.controller.config.CustomMockMvcConfigurer;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.StatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessInstanceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
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

  @Mock
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessInstanceService processInstanceService;

  @BeforeEach
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
    var processDefinition1 = ProcessDefinitionResponse.builder()
        .id("id1")
        .key("key1")
        .name("name1")
        .suspended(false)
        .formKey("formKey1")
        .build();
    var processDefinition2 = ProcessDefinitionResponse.builder()
        .id("id2")
        .key("key2")
        .name("name2")
        .suspended(true)
        .formKey("formKey2")
        .build();

    lenient()
        .when(processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams()))
        .thenReturn(List.of(processDefinition1, processDefinition2));
  }

  private void initGetProcessDefinitionByIdResponse() {
    var processDefinition = ProcessDefinitionResponse.builder()
        .id("id1")
        .key("key1")
        .name("name1")
        .suspended(false)
        .formKey("formKey1")
        .build();

    lenient()
        .when(processDefinitionService.getProcessDefinitionByKey("processDefinitionKey"))
        .thenReturn(processDefinition);
  }

  private void initCountProcessDefinitionsResponse() {
    lenient()
        .when(processDefinitionService.countProcessDefinitions(new GetProcessDefinitionsParams()))
        .thenReturn(new CountResponse(2L));
  }

  private void initStartProcessInstanceResponse() {
    lenient()
        .when(processDefinitionService.startProcessInstance(eq("processDefinitionKey"), any()))
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
        .when(processDefinitionService.startProcessInstanceWithForm(eq("processDefinitionKey"),
            any(), any()))
        .thenReturn(expectedResponse);
  }

  private void initCountProcessInstancesResponse() {
    lenient()
        .when(processInstanceService.countProcessInstances())
        .thenReturn(new CountResponse(3L));
  }

  private void initGetOfficerProcessInstancesResponse() {
    var processInstance1 = GetProcessInstanceResponse.builder()
        .id("id1")
        .processDefinitionName("name1")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .status(StatusModel.builder()
            .code(UserProcessInstanceStatus.SUSPENDED)
            .build())
        .build();
    var processInstance2 = GetProcessInstanceResponse.builder()
        .id("id2")
        .processDefinitionName("name2")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
        .status(StatusModel.builder()
            .code(UserProcessInstanceStatus.PENDING)
            .build())
        .build();
    lenient()
        .when(processInstanceService.getOfficerProcessInstances(new Pageable()))
        .thenReturn(List.of(processInstance1, processInstance2));
  }

  private void initGetCitizenProcessInstancesResponse() {
    var processInstance1 = GetProcessInstanceResponse.builder()
        .id("id3")
        .processDefinitionName("name3")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 0))
        .status(StatusModel.builder()
            .code(UserProcessInstanceStatus.SUSPENDED)
            .build())
        .build();
    var processInstance2 = GetProcessInstanceResponse.builder()
        .id("id4")
        .processDefinitionName("name4")
        .startTime(LocalDateTime.of(2020, 12, 1, 12, 1))
        .status(StatusModel.builder()
            .code(UserProcessInstanceStatus.PENDING)
            .build())
        .build();
    lenient()
        .when(processInstanceService.getCitizenProcessInstances(new Pageable()))
        .thenReturn(List.of(processInstance1, processInstance2));
  }
}
