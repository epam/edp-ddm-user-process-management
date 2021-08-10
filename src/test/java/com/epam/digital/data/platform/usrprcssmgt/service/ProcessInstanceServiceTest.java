package com.epam.digital.data.platform.usrprcssmgt.service;

import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.COMPLETED;
import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.EXTERNALLY_TERMINATED;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_COMPLETED;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.HistoryVariableInstanceClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceServiceTest {

  @Mock
  private ProcessInstanceRestClient processInstanceRestClient;
  @Mock
  private ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  @Mock
  private HistoryVariableInstanceClient historyVariableInstanceClient;
  @Mock
  private CamundaTaskRestClient camundaTaskRestClient;
  @Mock
  private MessageResolver messageResolver;
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(ProcessInstanceMapper.class);

  @InjectMocks
  private ProcessInstanceService processInstanceService;
  @InjectMocks
  private HistoryProcessInstanceService historyProcessInstanceService;

  @Test
  public void countProcessInstances() {
    var expectedCountDto = new CountResultDto(7L);
    when(processInstanceRestClient.getProcessInstancesCount(
        ProcessInstanceCountQueryDto.builder()
            .rootProcessInstances(true)
            .build()
    )).thenReturn(expectedCountDto);

    var result = processInstanceService.countProcessInstances();

    assertThat(result, is(expectedCountDto));
  }

  @Test
  public void countHistoryProcessInstances() {
    var expectedCountDto = new CountResultDto(420L);

    when(processInstanceHistoryRestClient.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
            .finished(true)
            .rootProcessInstances(true)
            .build())).thenReturn(expectedCountDto);

    var result = historyProcessInstanceService.getCountProcessInstances();

    assertThat(result, is(expectedCountDto));
  }

  @Test
  public void getProcessInstances() {
    var dateTime = LocalDateTime.of(2020, 12, 1, 12, 0);

    var historicProcessInstance1 = new HistoricProcessInstanceEntity();
    historicProcessInstance1.setId("id1");
    historicProcessInstance1.setProcessDefinitionName("name1");
    historicProcessInstance1.setStartTime(new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    var historicProcessInstance2 = new HistoricProcessInstanceEntity();
    historicProcessInstance2.setId("id2");
    historicProcessInstance2.setProcessDefinitionName("name2");
    historicProcessInstance2.setStartTime(null);
    var historyDtoSet = ImmutableList.of(
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance1),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance2));

    when(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).unfinished(true)
            .firstResult(10)
            .maxResults(42)
            .sortBy("dueDate")
            .sortOrder("asc").build()))
        .thenReturn(historyDtoSet);

    var result = processInstanceService.getOfficerProcessInstances(Pageable.builder()
        .firstResult(10)
        .maxResults(42)
        .sortBy("dueDate")
        .sortOrder("asc")
        .build());

    assertThat(result, hasSize(2));
    assertThat(result.get(0).getId(), is("id1"));
    assertThat(result.get(0).getProcessDefinitionName(), is("name1"));
    assertThat(result.get(0).getStartTime(), is(dateTime));
    assertThat(result.get(1).getId(), is("id2"));
    assertThat(result.get(1).getProcessDefinitionName(), is("name2"));
    assertNull(result.get(1).getStartTime());
  }

  @Test
  public void getHistoryProcessInstances() {
    var startDateTime = LocalDateTime.of(2020, 12, 1, 12, 0);
    var endDateTime = LocalDateTime.of(2020, 12, 2, 12, 0);

    var historicProcessInstance1 = createHistoricProcessInstanceEntity("id1", STATE_EXTERNALLY_TERMINATED,
        startDateTime, endDateTime);
    var historicProcessInstance2 = createHistoricProcessInstanceEntity("id2", STATE_COMPLETED, startDateTime,
        endDateTime);
    var historicProcessInstance3 = createHistoricProcessInstanceEntity("id3", "illegal", startDateTime,
        endDateTime);
    var historyDtoSet = ImmutableList.of(
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance1),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance2),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance3));

    when(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).finished(true)
            .sortBy("name")
            .sortOrder("desk")
            .maxResults(10)
            .firstResult(3).build()))
        .thenReturn(historyDtoSet);

    var externallyTerminatedTitle = "externally terminated";
    when(messageResolver.getMessage(EXTERNALLY_TERMINATED)).thenReturn(externallyTerminatedTitle);
    var completedTitle = "completed";
    when(messageResolver.getMessage(COMPLETED)).thenReturn(completedTitle);

    var result = historyProcessInstanceService.getHistoryProcessInstances(
        Pageable.builder()
            .sortBy("name")
            .sortOrder("desk")
            .maxResults(10)
            .firstResult(3)
            .build());

    assertThat(result, hasSize(3));
    assertThat(result.get(0).getId(), is("id1"));
    assertThat(result.get(0).getProcessDefinitionName(), is("name1"));
    assertThat(result.get(0).getStartTime(), is(startDateTime));
    assertThat(result.get(0).getEndTime(), is(endDateTime));
    assertThat(result.get(0).getStatus().getCode(), is(STATE_EXTERNALLY_TERMINATED));
    assertThat(result.get(0).getStatus().getTitle(), is(externallyTerminatedTitle));

    assertThat(result.get(1).getId(), is("id2"));
    assertThat(result.get(1).getProcessDefinitionName(), is("name1"));
    assertThat(result.get(1).getStartTime(), is(startDateTime));
    assertThat(result.get(1).getEndTime(), is(endDateTime));
    assertThat(result.get(1).getStatus().getCode(), is(STATE_COMPLETED));
    assertThat(result.get(1).getStatus().getTitle(), is(completedTitle));

    assertThat(result.get(2).getId(), is("id3"));
    assertThat(result.get(2).getProcessDefinitionName(), is("name1"));
    assertThat(result.get(2).getStartTime(), is(startDateTime));
    assertThat(result.get(2).getEndTime(), is(endDateTime));
    assertThat(result.get(2).getStatus().getCode(), is("illegal"));
    assertThat(result.get(2).getStatus().getTitle(), is(nullValue()));
  }

  private HistoricProcessInstanceEntity createHistoricProcessInstanceEntity(String id, String state,
                                                                            LocalDateTime startDateTime,
                                                                            LocalDateTime endDateTime) {
    var historicProcessInstance = new HistoricProcessInstanceEntity();
    historicProcessInstance.setId(id);
    historicProcessInstance.setProcessDefinitionName("name1");
    historicProcessInstance.setStartTime(new Date(startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    historicProcessInstance.setEndTime(new Date(endDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    historicProcessInstance.setState(state);
    return historicProcessInstance;
  }

  @Test
  public void getHistoryProcessInstancesById() {
    var startDateTime = LocalDateTime.of(2020, 12, 1, 12, 0);
    var endDateTime = LocalDateTime.of(2020, 12, 2, 12, 0);
    var testId = "testId";
    var historicProcessInstance = createHistoricProcessInstanceEntity(testId, STATE_COMPLETED, startDateTime,
        endDateTime);
    var historyDtoSet =
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance);

    when(processInstanceHistoryRestClient.getProcessInstanceById(testId))
        .thenReturn(historyDtoSet);

    var variable = new HistoricVariableInstanceDto();
    ReflectionTestUtils.setField(variable, "processInstanceId", testId);
    ReflectionTestUtils.setField(variable, "name", Constants.SYS_VAR_PROCESS_COMPLETION_RESULT);
    var variableValue = "completed";
    variable.setValue(variableValue);
    when(historyVariableInstanceClient.getList(
        HistoryVariableInstanceQueryDto.builder()
            .processInstanceIdIn(Collections.singletonList(testId))
            .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE).build()))
        .thenReturn(Collections.singletonList(variable));

    var result = historyProcessInstanceService.getHistoryProcessInstanceById(testId);

    assertThat(result.getId(), is(testId));
    assertThat(result.getStartTime(), is(startDateTime));
    assertThat(result.getEndTime(), is(endDateTime));
    assertThat(result.getStatus().getCode(), is(STATE_COMPLETED));
    assertThat(result.getStatus().getTitle(), is(variableValue));
  }
}
