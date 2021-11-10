package com.epam.digital.data.platform.usrprcssmgt.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceServiceTest {

  @InjectMocks
  private ProcessInstanceService processInstanceService;
  @Mock
  private ProcessInstanceRestClient processInstanceRestClient;
  @Mock
  private ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  @Mock
  private CamundaTaskRestClient camundaTaskRestClient;
  @Mock
  private MessageResolver messageResolver;
  @Spy
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

    var result = processInstanceService.countProcessInstances();

    assertThat(result.getCount(), is(7L));
  }

  @Test
  void getProcessInstances() {
    var dateTime = LocalDateTime.of(2020, 12, 1, 12, 0);

    var historicProcessInstance1 = new HistoricProcessInstanceEntity();
    historicProcessInstance1.setId("id1");
    historicProcessInstance1.setProcessDefinitionName("name1");
    historicProcessInstance1.setStartTime(
        new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    var historicProcessInstance2 = new HistoricProcessInstanceEntity();
    historicProcessInstance2.setId("id2");
    historicProcessInstance2.setProcessDefinitionName("name2");
    historicProcessInstance2.setStartTime(null);
    var historyDtoSet = ImmutableList.of(
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance1),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance2));

    when(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder()
            .rootProcessInstances(true)
            .unfinished(true)
            .firstResult(10)
            .maxResults(42)
            .sortBy("dueDate")
            .sortOrder("asc")
            .build()))
        .thenReturn(historyDtoSet);

    var officer = processInstanceService.getOfficerProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build());

    assertThat(officer, hasSize(2));
    assertThat(officer.get(0).getId(), is("id1"));
    assertThat(officer.get(0).getProcessDefinitionName(), is("name1"));
    assertThat(officer.get(0).getStartTime(), is(dateTime));
    assertThat(officer.get(1).getId(), is("id2"));
    assertThat(officer.get(1).getProcessDefinitionName(), is("name2"));
    assertNull(officer.get(1).getStartTime());

    var citizen = processInstanceService.getCitizenProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build());
    assertThat(citizen, hasSize(2));
    assertThat(citizen.get(0).getId(), is("id1"));
    assertThat(citizen.get(0).getProcessDefinitionName(), is("name1"));
    assertThat(citizen.get(0).getStartTime(), is(dateTime));
    assertThat(citizen.get(1).getId(), is("id2"));
    assertThat(citizen.get(1).getProcessDefinitionName(), is("name2"));
    assertNull(citizen.get(1).getStartTime());
  }
}
