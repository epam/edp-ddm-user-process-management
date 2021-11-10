package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.StatusModel;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessInstanceApi}. A proxy to the {@link
 * ProcessInstanceRestClient} that also maps the camunda response to the needed format and localizes
 * it if needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService implements ProcessInstanceApi {

  private final ProcessInstanceRestClient processInstanceRestClient;
  private final ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  private final CamundaTaskRestClient taskClient;

  private final ProcessInstanceMapper processInstanceMapper;

  private final MessageResolver messageResolver;

  @Override
  public CountResultDto countProcessInstances() {
    log.info("Getting count of unfinished process instances");

    var queryDto = ProcessInstanceCountQueryDto.builder()
        .rootProcessInstances(true)
        .build();
    var result = processInstanceRestClient.getProcessInstancesCount(queryDto);

    log.info("Count of unfinished process instances is found - {}", result.getCount());
    return result;
  }

  @Override
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page) {
    log.info("Getting unfinished officer process instances. Parameters: {}", page);

    var result = getProcessInstances(page, this::getOfficerProcessInstanceStatus);

    log.info("Found {} unfinished officer process instances", result.size());
    return result;
  }

  @Override
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page) {
    log.info("Getting unfinished citizen process instances. Parameters: {}", page);

    var result = getProcessInstances(page, this::getCitizenProcessInstanceStatus);

    log.info("Found {} unfinished citizen process instances", result.size());
    return result;
  }

  private List<GetProcessInstanceResponse> getProcessInstances(
      Pageable page,
      BiFunction<Boolean, Boolean, ProcessInstanceStatus> defineProcessInstanceStatusFunction) {

    var processInstances = getCamundaProcessInstances(page);
    log.trace("Found {} running camunda process instances", processInstances.size());

    var result = mapToGetProcessInstanceResponse(processInstances,
        defineProcessInstanceStatusFunction);
    log.trace("Found process instances - {}", result);

    return result;
  }

  private List<HistoricProcessInstanceDto> getCamundaProcessInstances(Pageable page) {
    var processInstanceQueryDto = HistoryProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .unfinished(true)
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    return processInstanceHistoryRestClient.getProcessInstances(processInstanceQueryDto);
  }

  /**
   * Fill camunda process instance object with additional data, such as custom process status
   */
  private List<GetProcessInstanceResponse> mapToGetProcessInstanceResponse(
      List<HistoricProcessInstanceDto> processInstances,
      BiFunction<Boolean, Boolean, ProcessInstanceStatus> defineProcessInstanceStatusFunction) {
    var processInstanceIds = extractProcessInstanceIds(processInstances);
    var activeTaskCounts = getActiveTaskCounts(processInstanceIds);

    return processInstances.stream().map(pi -> {
      var isSuspended = HistoricProcessInstance.STATE_SUSPENDED.equals(pi.getState());
      var hasActiveTasks = activeTaskCounts.getOrDefault(pi.getId(), 0L) > 0;
      var status = defineProcessInstanceStatusFunction.apply(isSuspended, hasActiveTasks);

      var resultPi = processInstanceMapper.toGetProcessInstanceResponse(pi);

      log.trace("Setting {} status to process instance {}", status, resultPi.getId());
      resultPi.setStatus(new StatusModel(status.name(), messageResolver.getMessage(status)));

      return resultPi;
    }).collect(Collectors.toList());
  }

  private List<String> extractProcessInstanceIds(
      List<HistoricProcessInstanceDto> processInstances) {
    return processInstances.stream()
        .map(HistoricProcessInstanceDto::getId)
        .collect(Collectors.toList());
  }

  private Map<String, Long> getActiveTaskCounts(List<String> processInstanceIds) {
    var taskQueryDto = TaskQueryDto.builder()
        .processInstanceIdIn(processInstanceIds)
        .build();
    var paginationQueryDto = PaginationQueryDto.builder().build();
    log.debug("Selecting tasks for process instances {}", processInstanceIds);
    var result = taskClient.getTasksByParams(taskQueryDto, paginationQueryDto).stream()
        .collect(Collectors.groupingBy(TaskDto::getProcessInstanceId, Collectors.counting()));
    log.debug("Selected task counts {}", result);
    return result;
  }

  private ProcessInstanceStatus getOfficerProcessInstanceStatus(boolean isSuspended,
      boolean hasActiveTasks) {
    if (isSuspended) {
      return ProcessInstanceStatus.SUSPENDED;
    }
    return hasActiveTasks ? ProcessInstanceStatus.PENDING : ProcessInstanceStatus.IN_PROGRESS;
  }

  private ProcessInstanceStatus getCitizenProcessInstanceStatus(boolean isSuspended,
      boolean hasActiveTasks) {
    if (isSuspended) {
      return ProcessInstanceStatus.CITIZEN_SUSPENDED;
    }
    return hasActiveTasks ? ProcessInstanceStatus.CITIZEN_PENDING
        : ProcessInstanceStatus.CITIZEN_IN_PROGRESS;
  }
}
