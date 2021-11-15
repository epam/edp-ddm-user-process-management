package com.epam.digital.data.platform.usrprcssmgt.service;

import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.COMPLETED;
import static com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus.EXTERNALLY_TERMINATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_COMPLETED;
import static org.camunda.bpm.engine.history.HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.bpms.client.HistoryProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.mapper.HistoryProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryStatusModel;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoryProcessInstanceServiceTest {

  @InjectMocks
  private HistoryProcessInstanceService historyProcessInstanceService;
  @Mock
  private HistoryProcessInstanceRestClient historyProcessInstanceRestClient;
  @Mock
  private MessageResolver messageResolver;
  @Spy
  @InjectMocks
  private HistoryProcessInstanceMapper historyProcessInstanceMapper = Mappers.getMapper(
      HistoryProcessInstanceMapper.class);

  @Test
  void countHistoryProcessInstances() {
    var expectedCountDto = new CountResultDto(420L);

    when(historyProcessInstanceRestClient.getProcessInstancesCount(
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

    var historyProcessInstance1 = createHistoricProcessInstanceEntity("id1",
        HistoryProcessInstanceStatus.EXTERNALLY_TERMINATED, startDateTime, endDateTime, null, null);
    var historyProcessInstance2 = createHistoricProcessInstanceEntity("id2",
        HistoryProcessInstanceStatus.COMPLETED, startDateTime, endDateTime, null, null);
    var historyProcessInstance3 = createHistoricProcessInstanceEntity("id3", null,
        startDateTime, endDateTime, null, null);
    var historyDtoSet = List.of(historyProcessInstance1, historyProcessInstance2,
        historyProcessInstance3);

    when(historyProcessInstanceRestClient.getHistoryProcessInstanceDtosByParams(
        HistoryProcessInstanceQueryDto.builder()
            .rootProcessInstances(true)
            .finished(true)
            .sortBy("name")
            .sortOrder("desk")
            .build(), PaginationQueryDto.builder()
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
            HistoryStatusModel.builder().code(null).title(null).build());
  }

  @Test
  void getHistoryProcessInstancesById() {
    var startDateTime = LocalDateTime.of(2020, 12, 1, 12, 0);
    var endDateTime = LocalDateTime.of(2020, 12, 2, 12, 0);
    var testId = "testId";
    var historyProcessInstance = createHistoricProcessInstanceEntity(testId,
        HistoryProcessInstanceStatus.COMPLETED,
        startDateTime,
        endDateTime,
        "completed",
        "excerptId");

    when(historyProcessInstanceRestClient.getProcessInstanceById(testId))
        .thenReturn(historyProcessInstance);

    var result = historyProcessInstanceService.getHistoryProcessInstanceById(testId);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", testId)
        .hasFieldOrPropertyWithValue("startTime", startDateTime)
        .hasFieldOrPropertyWithValue("endTime", endDateTime)
        .hasFieldOrPropertyWithValue("excerptId", "excerptId")
        .hasFieldOrPropertyWithValue("status",
            HistoryStatusModel.builder().code(STATE_COMPLETED).title("completed").build());
  }

  private HistoryProcessInstanceDto createHistoricProcessInstanceEntity(String id,
      HistoryProcessInstanceStatus state,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String processCompletionResult,
      String excerptId) {
    var historicProcessInstance = new HistoryProcessInstanceDto();
    historicProcessInstance.setId(id);
    historicProcessInstance.setProcessDefinitionName("name1");
    historicProcessInstance.setStartTime(startDateTime);
    historicProcessInstance.setEndTime(endDateTime);
    historicProcessInstance.setState(state);
    historicProcessInstance.setProcessCompletionResult(processCompletionResult);
    historicProcessInstance.setExcerptId(excerptId);
    return historicProcessInstance;
  }
}
