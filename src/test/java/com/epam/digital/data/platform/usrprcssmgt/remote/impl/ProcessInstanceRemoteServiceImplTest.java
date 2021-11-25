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
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.client.HistoryProcessInstanceRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.i18n.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.mapper.BaseMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceRemoteServiceImplTest {

  @InjectMocks
  private ProcessInstanceRemoteServiceImpl processInstanceRemoteService;
  @Mock
  private ProcessInstanceRestClient processInstanceRestClient;
  @Mock
  private HistoryProcessInstanceRestClient historyProcessInstanceRestClient;
  @Mock
  private MessageResolver messageResolver;
  @Spy
  private BaseMapper baseMapper = Mappers.getMapper(BaseMapper.class);
  @Spy
  @InjectMocks
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Test
  void countProcessInstances() {
    var expectedCountDto = new CountResultDto(7L);
    when(processInstanceRestClient.getProcessInstancesCount(
        ProcessInstanceCountQueryDto.builder()
            .rootProcessInstances(true)
            .build()
    )).thenReturn(expectedCountDto);

    var result = processInstanceRemoteService.countProcessInstances();

    assertThat(result)
        .hasFieldOrPropertyWithValue("count", 7L);
  }

  @Test
  void getProcessInstances() {
    var dateTime = LocalDateTime.of(2020, 12, 1, 12, 0);

    var historyProcessInstance1 = new HistoryProcessInstanceDto();
    historyProcessInstance1.setId("id1");
    historyProcessInstance1.setProcessDefinitionName("name1");
    historyProcessInstance1.setStartTime(dateTime);
    historyProcessInstance1.setState(HistoryProcessInstanceStatus.PENDING);
    var historyProcessInstance2 = new HistoryProcessInstanceDto();
    historyProcessInstance2.setId("id2");
    historyProcessInstance2.setProcessDefinitionName("name2");
    historyProcessInstance2.setStartTime(null);
    historyProcessInstance2.setState(HistoryProcessInstanceStatus.ACTIVE);
    var historyDtoSet = List.of(historyProcessInstance1, historyProcessInstance2);

    when(historyProcessInstanceRestClient.getHistoryProcessInstanceDtosByParams(
        HistoryProcessInstanceQueryDto.builder()
            .rootProcessInstances(true)
            .unfinished(true)
            .sortBy("dueDate")
            .sortOrder("asc")
            .build(), PaginationQueryDto.builder()
            .firstResult(10)
            .maxResults(42)
            .build()))
        .thenReturn(historyDtoSet);

    when(messageResolver.getMessage(ProcessInstanceStatus.PENDING))
        .thenReturn("officer pending title");
    when(messageResolver.getMessage(ProcessInstanceStatus.IN_PROGRESS))
        .thenReturn("officer in progress title");
    var officer = processInstanceRemoteService.getOfficerProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build());

    assertThat(officer).hasSize(2);
    assertThat(officer.get(0))
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", dateTime)
        .hasFieldOrPropertyWithValue("status", GetProcessInstanceResponse.StatusModel.builder()
            .code(ProcessInstanceStatus.PENDING)
            .title("officer pending title")
            .build());
    assertThat(officer.get(1))
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name2")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("status", GetProcessInstanceResponse.StatusModel.builder()
            .code(ProcessInstanceStatus.IN_PROGRESS)
            .title("officer in progress title")
            .build());

    when(messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_PENDING))
        .thenReturn("citizen pending title");
    when(messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_IN_PROGRESS))
        .thenReturn("citizen in progress title");
    var citizen = processInstanceRemoteService.getCitizenProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build());
    assertThat(citizen).hasSize(2);
    assertThat(citizen.get(0))
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", dateTime)
        .hasFieldOrPropertyWithValue("status", GetProcessInstanceResponse.StatusModel.builder()
            .code(ProcessInstanceStatus.CITIZEN_PENDING)
            .title("citizen pending title")
            .build());
    assertThat(citizen.get(1))
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name2")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("status", GetProcessInstanceResponse.StatusModel.builder()
            .code(ProcessInstanceStatus.CITIZEN_IN_PROGRESS)
            .title("citizen in progress title")
            .build());
  }
}
