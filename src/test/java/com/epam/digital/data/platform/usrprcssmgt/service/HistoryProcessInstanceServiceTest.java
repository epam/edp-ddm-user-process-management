package com.epam.digital.data.platform.usrprcssmgt.service;

import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.COMPLETED;
import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.EXTERNALLY_TERMINATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_COMPLETED;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryVariableInstanceClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryStatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HistoryProcessInstanceServiceTest {

  @InjectMocks
  private HistoryProcessInstanceService historyProcessInstanceService;
  @Mock
  private ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  @Mock
  private HistoryVariableInstanceClient historyVariableInstanceClient;
  @Mock
  private MessageResolver messageResolver;
  @Spy
  private ProcessInstanceMapper processInstanceMapper = Mappers.getMapper(
      ProcessInstanceMapper.class);

  @Test
  void countHistoryProcessInstances() {
    var expectedCountDto = new CountResultDto(420L);

    when(processInstanceHistoryRestClient.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
            .finished(true)
            .rootProcessInstances(true)
            .build())).thenReturn(expectedCountDto);

    var result = historyProcessInstanceService.getCountProcessInstances();

    assertThat(result.getCount()).isEqualTo(420L);
  }

  @Test
  void getHistoryProcessInstances() {
    var startDateTime = LocalDateTime.of(2020, 12, 1, 12, 0);
    var endDateTime = LocalDateTime.of(2020, 12, 2, 12, 0);

    var historicProcessInstance1 = createHistoricProcessInstanceEntity("id1",
        STATE_EXTERNALLY_TERMINATED, startDateTime, endDateTime);
    var historicProcessInstance2 = createHistoricProcessInstanceEntity("id2", STATE_COMPLETED,
        startDateTime, endDateTime);
    var historicProcessInstance3 = createHistoricProcessInstanceEntity("id3", "illegal",
        startDateTime, endDateTime);
    var historyDtoSet = List.of(
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance1),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance2),
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance3));

    when(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder()
            .rootProcessInstances(true)
            .finished(true)
            .sortBy("name")
            .sortOrder("desk")
            .maxResults(10)
            .firstResult(3)
            .build()))
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

    MatcherAssert.assertThat(result, hasSize(3));
    assertThat(result).hasSize(3);
    assertThat(result.get(0))
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", startDateTime)
        .hasFieldOrPropertyWithValue("endTime", endDateTime)
        .hasFieldOrPropertyWithValue("status",
            HistoryStatusModel.builder().code(STATE_EXTERNALLY_TERMINATED)
                .title(externallyTerminatedTitle).build());

    assertThat(result.get(1))
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", startDateTime)
        .hasFieldOrPropertyWithValue("endTime", endDateTime)
        .hasFieldOrPropertyWithValue("status",
            HistoryStatusModel.builder().code(STATE_COMPLETED)
                .title(completedTitle).build());

    assertThat(result.get(2))
        .hasFieldOrPropertyWithValue("id", "id3")
        .hasFieldOrPropertyWithValue("processDefinitionName", "name1")
        .hasFieldOrPropertyWithValue("startTime", startDateTime)
        .hasFieldOrPropertyWithValue("endTime", endDateTime)
        .hasFieldOrPropertyWithValue("status",
            HistoryStatusModel.builder().code("illegal").title(null).build());
  }

  @Test
  void getHistoryProcessInstancesById() {
    var startDateTime = LocalDateTime.of(2020, 12, 1, 12, 0);
    var endDateTime = LocalDateTime.of(2020, 12, 2, 12, 0);
    var testId = "testId";
    var historicProcessInstance = createHistoricProcessInstanceEntity(testId, STATE_COMPLETED,
        startDateTime,
        endDateTime);
    var historyDtoSet =
        HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance);

    when(processInstanceHistoryRestClient.getProcessInstanceById(testId))
        .thenReturn(historyDtoSet);

    var variable = new HistoricVariableInstanceDto();
    ReflectionTestUtils.setField(variable, "processInstanceId", testId);
    ReflectionTestUtils.setField(variable, "name",
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
    var variableValue = "completed";
    variable.setValue(variableValue);
    var excerptIdVariable = new HistoricVariableInstanceDto();
    ReflectionTestUtils.setField(excerptIdVariable, "processInstanceId", testId);
    ReflectionTestUtils.setField(excerptIdVariable, "name",
        ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
    var excerptIdVariableValue = "excerptId";
    excerptIdVariable.setValue(excerptIdVariableValue);

    when(historyVariableInstanceClient.getList(
        HistoryVariableInstanceQueryDto.builder()
            .processInstanceId(testId)
            .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE).build()))
        .thenReturn(List.of(variable, excerptIdVariable));

    var result = historyProcessInstanceService.getHistoryProcessInstanceById(testId);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", testId)
        .hasFieldOrPropertyWithValue("startTime", startDateTime)
        .hasFieldOrPropertyWithValue("endTime", endDateTime)
        .hasFieldOrPropertyWithValue("excerptId", excerptIdVariableValue)
        .hasFieldOrPropertyWithValue("status",
            HistoryStatusModel.builder().code(STATE_COMPLETED).title(variableValue).build());
  }

  private HistoricProcessInstanceEntity createHistoricProcessInstanceEntity(String id, String state,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    var historicProcessInstance = new HistoricProcessInstanceEntity();
    historicProcessInstance.setId(id);
    historicProcessInstance.setProcessDefinitionName("name1");
    historicProcessInstance.setStartTime(
        new Date(startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    historicProcessInstance.setEndTime(
        new Date(endDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
    historicProcessInstance.setState(state);
    return historicProcessInstance;
  }
}
