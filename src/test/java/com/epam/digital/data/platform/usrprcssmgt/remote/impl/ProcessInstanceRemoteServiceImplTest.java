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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.DdmProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.starter.security.SystemRole;
import com.epam.digital.data.platform.usrprcssmgt.i18n.ProcessInstanceStatusMessageTitle;
import com.epam.digital.data.platform.usrprcssmgt.mapper.BaseMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.StatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
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
        DdmProcessInstanceCountQueryDto.builder()
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

    var processInstance1 = DdmProcessInstanceDto.builder()
        .id("id1")
        .processDefinitionName("name1")
        .startTime(dateTime)
        .state(DdmProcessInstanceStatus.PENDING)
        .build();
    var processInstance2 = DdmProcessInstanceDto.builder()
        .id("id2")
        .processDefinitionName("name2")
        .startTime(null)
        .state(DdmProcessInstanceStatus.ACTIVE)
        .build();
    var dtoSet = List.of(processInstance1, processInstance2);

    when(processInstanceRestClient.getProcessInstances(
        DdmProcessInstanceQueryDto.builder()
            .rootProcessInstances(true)
            .sortBy("dueDate")
            .sortOrder("asc")
            .build(), PaginationQueryDto.builder()
            .firstResult(10)
            .maxResults(42)
            .build()))
        .thenReturn(dtoSet);

    when(messageResolver.getMessage(ProcessInstanceStatusMessageTitle.PENDING))
        .thenReturn("officer pending title");
    when(messageResolver.getMessage(ProcessInstanceStatusMessageTitle.IN_PROGRESS))
        .thenReturn("officer in progress title");
    var officer = processInstanceRemoteService.getProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build(), SystemRole.OFFICER);

    assertThat(officer).hasSize(2);
    assertThat(officer.get(0))
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", dateTime)
        .hasFieldOrPropertyWithValue("status", StatusModel.builder()
            .code(UserProcessInstanceStatus.PENDING)
            .title("officer pending title")
            .build());
    assertThat(officer.get(1))
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name2")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("status", StatusModel.builder()
            .code(UserProcessInstanceStatus.ACTIVE)
            .title("officer in progress title")
            .build());

    when(messageResolver.getMessage(ProcessInstanceStatusMessageTitle.CITIZEN_PENDING))
        .thenReturn("citizen pending title");
    when(messageResolver.getMessage(ProcessInstanceStatusMessageTitle.CITIZEN_IN_PROGRESS))
        .thenReturn("citizen in progress title");
    var citizen = processInstanceRemoteService.getProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build(), SystemRole.CITIZEN);
    assertThat(citizen).hasSize(2);
    assertThat(citizen.get(0))
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", dateTime)
        .hasFieldOrPropertyWithValue("status", StatusModel.builder()
            .code(UserProcessInstanceStatus.PENDING)
            .title("citizen pending title")
            .build());
    assertThat(citizen.get(1))
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name2")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("status", StatusModel.builder()
            .code(UserProcessInstanceStatus.ACTIVE)
            .title("citizen in progress title")
            .build());
  }
}
