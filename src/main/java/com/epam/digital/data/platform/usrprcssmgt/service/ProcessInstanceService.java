package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.bpms.client.CamundaTaskRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
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
 * The class represents a service for {@link GetProcessInstanceResponse} entity and contains methods
 * for working with an unfinished process instance.
 * <p>
 * The ProcessInstanceService class provides a method to get the number of unfinished process
 * instances
 * <p>
 * The ProcessInstanceService class provides a method to get list of unfinished process instances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService {

  private final ProcessInstanceRestClient processInstanceRestClient;
  private final ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  private final CamundaTaskRestClient taskClient;
  private final ProcessInstanceMapper processInstanceMapper;
  private final MessageResolver messageResolver;

  /**
   * Method for getting the number of unfinished process instances with root process instance
   *
   * @return an entity that defines the number of unfinished process instances.
   */
  public CountResultDto countProcessInstances() {
    log.info("Getting count of unfinished process instances");

    var result = processInstanceRestClient.getProcessInstancesCount(
        ProcessInstanceCountQueryDto.builder()
            .rootProcessInstances(true)
            .build());

    log.info("Count of unfinished process instances is found - {}", result.getCount());
    return result;
  }

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the role of an officer.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list of unfinished process instances.
   */
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page) {
    return getProcessInstances(page, this::getOfficerProcessInstanceStatus);
  }

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the citizen role.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list of unfinished process instances.
   */
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page) {
    return getProcessInstances(page, this::getCitizenProcessInstanceStatus);
  }

  private List<GetProcessInstanceResponse> getProcessInstances(
      Pageable page,
      BiFunction<Boolean, Boolean, ProcessInstanceStatus> defineProcessInstanceStatusFunction) {
    log.info("Getting unfinished process instances. Parameters: {}", page);

    var processInstanceQueryDto = buildProcessInstanceQueryDto(page);
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(processInstanceQueryDto);
    var processInstanceIds = extractProcessInstanceIds(processInstances);
    var activeTaskCounts = getActiveTaskCounts(processInstanceIds);

    var result = processInstances.stream().map(pi -> {
      var isSuspended = HistoricProcessInstance.STATE_SUSPENDED.equals(pi.getState());
      var hasActiveTasks = activeTaskCounts.getOrDefault(pi.getId(), 0L) > 0;
      var status = defineProcessInstanceStatusFunction.apply(isSuspended, hasActiveTasks);

      var resultPi = processInstanceMapper.toGetProcessInstanceResponse(pi);

      log.trace("Setting {} status to process instance {}", status, resultPi.getId());
      resultPi.setStatus(new StatusModel(status.name(), messageResolver.getMessage(status)));

      return resultPi;
    }).collect(Collectors.toList());
    log.trace("Found process instances - {}", result);

    log.info("Found {} unfinished process instances. Ids - {}", result.size(), processInstanceIds);
    return result;
  }

  private HistoryProcessInstanceQueryDto buildProcessInstanceQueryDto(Pageable page) {
    return HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).unfinished(true)
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder()).build();
  }

  private List<String> extractProcessInstanceIds(List<HistoricProcessInstanceDto> processInstances) {
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

  protected ProcessInstanceStatus getOfficerProcessInstanceStatus(boolean isSuspended, boolean hasActiveTasks) {
    if (isSuspended) {
      return ProcessInstanceStatus.SUSPENDED;
    }
    return hasActiveTasks ? ProcessInstanceStatus.PENDING : ProcessInstanceStatus.IN_PROGRESS;
  }

  private ProcessInstanceStatus getCitizenProcessInstanceStatus(boolean isSuspended, boolean hasActiveTasks) {
    if (isSuspended) {
      return ProcessInstanceStatus.CITIZEN_SUSPENDED;
    }
    return hasActiveTasks ? ProcessInstanceStatus.CITIZEN_PENDING : ProcessInstanceStatus.CITIZEN_IN_PROGRESS;
  }
}
