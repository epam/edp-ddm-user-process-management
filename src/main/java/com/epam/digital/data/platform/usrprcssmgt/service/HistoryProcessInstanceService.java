package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryVariableInstanceClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.springframework.stereotype.Service;

/**
 * The HistoryProcessInstanceService class represents a service for {@link HistoryProcessInstance}
 * entity and contains methods for working with a finished process instance.
 * <p>
 * The HistoryProcessInstanceService class provides a method to get a list of finished process
 * instances
 * <p>
 * The HistoryProcessInstanceService class provides a method to get a finished process instance by
 * the specified identifier
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryProcessInstanceService {

  private final ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  private final HistoryVariableInstanceClient historyVariableInstanceClient;

  private final ProcessInstanceMapper processInstanceMapper;

  private final MessageResolver messageResolver;

  /**
   * Method for getting a list of finished process instance entities. The list must be sorted by end
   * time and in descending order.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list containing all the finished process instances.
   */
  public List<HistoryProcessInstance> getHistoryProcessInstances(Pageable page) {
    log.info("Getting finished process instances. Parameters: {}", page);

    var historyProcessInstanceQueryDto = buildHistoryProcessInstanceQueryDto(page);
    var historyProcessInstances = processInstanceHistoryRestClient.getProcessInstances(historyProcessInstanceQueryDto);
    var processInstanceIds = extractProcessInstanceIds(historyProcessInstances);
    var groupedProcessInstanceVariables = getGroupedProcessInstanceVariables(processInstanceIds);

    var result = historyProcessInstances.stream()
        .map(hpi -> mapToHistoryProcessInstance(hpi, groupedProcessInstanceVariables.getOrDefault(hpi.getId(), List.of())))
        .collect(Collectors.toList());
    log.trace("Found process instances - {}", result);
    log.info("{} process instances are found. Ids: {}", result.size(), processInstanceIds);
    return result;
  }

  /**
   * Method for getting finished process instance entity by id.
   *
   * @param processInstanceId process instance identifier.
   * @return finished process instance.
   */
  public HistoryProcessInstance getHistoryProcessInstanceById(String processInstanceId) {
    log.info("Get finished process instance by id {}", processInstanceId);

    var processInstance = processInstanceHistoryRestClient.getProcessInstanceById(processInstanceId);
    var processInstanceVariables = getProcessInstanceVariables(processInstanceId);

    var result = mapToHistoryProcessInstance(processInstance, processInstanceVariables);
    log.trace("Found process instance - {}", processInstance);
    log.info("Process instance {} is found successfully", processInstanceId);
    return result;
  }

  /**
   * Method for getting the number of root finished process instances
   *
   * @return an entity that defines the number of finished process instances.
   */
  public CountResultDto getCountProcessInstances() {
    log.info("Getting count of finished process instances");

    var countDto = processInstanceHistoryRestClient.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
            .finished(true)
            .rootProcessInstances(true)
            .build());

    log.info("Count of finished process instances is found - {}", countDto.getCount());
    return countDto;
  }

  private HistoryProcessInstanceQueryDto buildHistoryProcessInstanceQueryDto(Pageable page) {
    return HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).finished(true)
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults()).build();
  }

  private List<String> extractProcessInstanceIds(List<HistoricProcessInstanceDto> processInstances) {
    return processInstances.stream()
        .map(HistoricProcessInstanceDto::getId)
        .collect(Collectors.toList());
  }

  private Map<String, List<HistoricVariableInstanceDto>> getGroupedProcessInstanceVariables(
      List<String> processInstanceIds) {
    log.debug("Selecting all variables with prefix {} for processInstances {}", Constants.SYS_VAR_PREFIX_LIKE, processInstanceIds);
    var queryDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(processInstanceIds)
        .build();

    return historyVariableInstanceClient
        .getList(queryDto).stream()
        .collect(Collectors.groupingBy(HistoricVariableInstanceDto::getProcessInstanceId));
  }

  private List<HistoricVariableInstanceDto> getProcessInstanceVariables(String processInstanceId) {
    log.debug("Selecting all variables with prefix {} for processInstance {}", Constants.SYS_VAR_PREFIX_LIKE, processInstanceId);
    var queryDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceId(processInstanceId)
        .build();

    return historyVariableInstanceClient.getList(queryDto);
  }

  private HistoryProcessInstance mapToHistoryProcessInstance(
      HistoricProcessInstanceDto hpi, List<HistoricVariableInstanceDto> processInstanceVariables) {
    var variableNameVariableInstanceMap = processInstanceVariables
        .stream()
        .collect(Collectors.groupingBy(HistoricVariableInstanceDto::getName));

    var historyProcessInstance = processInstanceMapper.toHistoryProcessInstance(hpi);

    var excerptId = getExcerptId(variableNameVariableInstanceMap);
    log.trace("Setting excerptId={} to processInstance {}", excerptId, hpi.getId());
    historyProcessInstance.setExcerptId(excerptId);

    var statusTitle = getStatusTitle(historyProcessInstance, variableNameVariableInstanceMap);
    log.trace("Setting title={} to processInstance {}", statusTitle, hpi.getId());
    historyProcessInstance.getStatus().setTitle(statusTitle);

    return historyProcessInstance;
  }

  private String getExcerptId(Map<String, List<HistoricVariableInstanceDto>> variablesMap) {
    if (variablesMap.containsKey(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID)) {
      var excerptId = variablesMap.get(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID).get(0);
      return (String) excerptId.getValue();
    }
    return null;
  }

  private String getStatusTitle(HistoryProcessInstance processInstance,
                                Map<String, List<HistoricVariableInstanceDto>> variablesMap) {
    var code = processInstance.getStatus().getCode();
    if (StringUtils.equals(code, HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED)) {
      return messageResolver.getMessage(ProcessInstanceStatus.EXTERNALLY_TERMINATED);
    }

    if (!StringUtils.equals(code, HistoricProcessInstance.STATE_COMPLETED)) {
      return null;
    }

    return variablesMap.getOrDefault(
            ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT, List.of())
        .stream()
        .findFirst()
        .map(VariableValueDto::getValue)
        .map(Object::toString)
        .orElse(messageResolver.getMessage(ProcessInstanceStatus.COMPLETED));
  }
}
