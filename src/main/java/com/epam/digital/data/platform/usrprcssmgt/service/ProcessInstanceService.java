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
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The class represents a service for {@link GetProcessInstanceResponse} entity and contains methods
 * for working with an unfinished process instance.
 * <p>
 * The ProcessInstanceService class provides a method to get the number of unfinished process
 * instances
 * <p>
 * The ProcessInstanceService class provides a method to get list of unfinished process instances
 */
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
    return processInstanceRestClient.getProcessInstancesCount(
        ProcessInstanceCountQueryDto.builder()
            .rootProcessInstances(true)
            .build()
    );
  }

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the role of an officer.
   *
   * @return a list of unfinished process instances.
   * @param page defines the pagination parameters to shrink result lust
   */
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(
      Pageable page) {
    return postProcess(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).unfinished(true)
            .firstResult(page.getFirstResult())
            .maxResults(page.getMaxResults())
            .sortBy(page.getSortBy())
            .sortOrder(page.getSortOrder()).build()), this::getOfficerProcessInstanceStatus);
  }

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the citizen role.
   *
   * @return a list of unfinished process instances.
   * @param page defines the pagination parameters to shrink result lust
   */
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(
      Pageable page) {
    return postProcess(processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).unfinished(true)
            .firstResult(page.getFirstResult())
            .maxResults(page.getMaxResults())
            .sortBy(page.getSortBy())
            .sortOrder(page.getSortOrder()).build()), this::getCitizenProcessInstanceStatus);
  }

  private List<GetProcessInstanceResponse> postProcess(
      List<HistoricProcessInstanceDto> processInstances,
      BiFunction<HistoricProcessInstanceDto, Map<String, List<TaskDto>>, ProcessInstanceStatus> functionForStatus) {
    var processInstanceIdAndTasks = getProcessInstanceAndTasksMap(processInstances);
    return processInstances.stream().map(pi -> {
      var processInstance = processInstanceMapper.toGetProcessInstanceResponse(pi);
      var status = functionForStatus.apply(pi, processInstanceIdAndTasks);
      processInstance.setStatus(new StatusModel(status.name(), messageResolver.getMessage(status)));
      return processInstance;
    }).collect(Collectors.toList());
  }

  private Map<String, List<TaskDto>> getProcessInstanceAndTasksMap(
      List<HistoricProcessInstanceDto> processInstances) {
    List<String> processInstanceIds = processInstances.stream()
        .map(HistoricProcessInstanceDto::getId)
        .collect(Collectors.toList());
    var taskQueryDto = TaskQueryDto.builder()
        .processInstanceIdIn(processInstanceIds)
        .build();
    var paginationQueryDto = PaginationQueryDto.builder().build();
    return taskClient.getTasksByParams(taskQueryDto, paginationQueryDto).stream()
        .collect(Collectors.groupingBy(TaskDto::getProcessInstanceId));
  }

  private ProcessInstanceStatus getOfficerProcessInstanceStatus(
      HistoricProcessInstanceDto processInstance,
      Map<String, List<TaskDto>> processInstanceIdAndTasks) {
    if (HistoricProcessInstance.STATE_SUSPENDED.equals(processInstance.getState())) {
      return ProcessInstanceStatus.SUSPENDED;
    }
    if (hasActiveTasks(processInstance, processInstanceIdAndTasks)) {
      return ProcessInstanceStatus.PENDING;
    }
    return ProcessInstanceStatus.IN_PROGRESS;
  }

  private ProcessInstanceStatus getCitizenProcessInstanceStatus(
      HistoricProcessInstanceDto processInstance,
      Map<String, List<TaskDto>> processInstanceIdAndTasks) {
    if (HistoricProcessInstance.STATE_SUSPENDED.equals(processInstance.getState())) {
      return ProcessInstanceStatus.CITIZEN_SUSPENDED;
    }
    if (hasActiveTasks(processInstance, processInstanceIdAndTasks)) {
      return ProcessInstanceStatus.CITIZEN_PENDING;
    }
    return ProcessInstanceStatus.CITIZEN_IN_PROGRESS;
  }


  private boolean hasActiveTasks(HistoricProcessInstanceDto processInstance,
      Map<String, List<TaskDto>> processInstanceIdAndTasks) {
    return !CollectionUtils.isEmpty(processInstanceIdAndTasks.get(processInstance.getId()));
  }
}
