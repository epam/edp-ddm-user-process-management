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
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import java.util.Collections;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;

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
    var definition = DdmProcessDefinitionDto.builder()
        .id("id")
        .name("Awesome Definition Name")
        .formKey("testFormKey")
        .build();
    when(processDefinitionRestClient.getProcessDefinitionsByParams(processDefinitionQuery))
        .thenReturn(Collections.singletonList(definition));

    var result = processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams());

    assertThat(result).hasSize(1)
        .element(0).isSameAs(definition);
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

    var result = processDefinitionService.getProcessDefinitionByKey(processDefinitionKey);

    assertThat(result).isSameAs(definition);
  }
}
