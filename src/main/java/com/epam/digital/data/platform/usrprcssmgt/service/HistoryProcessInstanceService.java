package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryVariableInstanceClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceHistoryRestClient;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
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
@Service
@RequiredArgsConstructor
public class HistoryProcessInstanceService {

  private final ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  private final ProcessInstanceMapper processInstanceMapper;
  private final HistoryVariableInstanceClient historyVariableInstanceClient;
  private final MessageResolver messageResolver;

  /**
   * Method for getting a list of finished process instance entities. The list must be sorted by end
   * time and in descending order.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list containing all of the finished process instances.
   */
  public List<HistoryProcessInstance> getHistoryProcessInstances(Pageable page) {
    var processInstances = processInstanceMapper.toHistoryProcessInstances(
        processInstanceHistoryRestClient.getProcessInstances(
            HistoryProcessInstanceQueryDto.builder().rootProcessInstances(true).finished(true)
                .sortBy(page.getSortBy())
                .sortOrder(page.getSortOrder())
                .firstResult(page.getFirstResult())
                .maxResults(page.getMaxResults()).build()));
    postProcess(processInstances);
    return processInstances;
  }

  /**
   * Method for getting finished process instance entity by id.
   *
   * @param processInstanceId process instance identifier.
   * @return finished process instance.
   */
  public HistoryProcessInstance getHistoryProcessInstanceById(String processInstanceId) {
    var processInstance = processInstanceMapper.toHistoryProcessInstance(
        processInstanceHistoryRestClient.getProcessInstanceById(processInstanceId));
    postProcess(Collections.singletonList(processInstance));
    return processInstance;
  }

  /**
   * Method for getting the number of root finished process instances
   *
   * @return an entity that defines the number of finished process instances.
   */
  public CountResultDto getCountProcessInstances() {
    return processInstanceHistoryRestClient.getProcessInstancesCount(
        HistoryProcessInstanceCountQueryDto.builder()
            .finished(true)
            .rootProcessInstances(true)
            .build()
    );
  }

  private String buildTitle(String code, List<HistoricVariableInstanceDto> variables) {
    if (StringUtils.equals(code, HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED)) {
      return messageResolver.getMessage(ProcessInstanceStatus.EXTERNALLY_TERMINATED);
    }
    if (StringUtils.equals(code, HistoricProcessInstance.STATE_COMPLETED)) {
      return variables.stream()
          .findFirst()
          .map(VariableValueDto::getValue)
          .map(Object::toString)
          .orElse(messageResolver.getMessage(ProcessInstanceStatus.COMPLETED));
    }
    return null;
  }

  private void postProcess(List<HistoryProcessInstance> processInstances) {
    var processInstanceIds = processInstances.stream().map(HistoryProcessInstance::getId)
        .collect(Collectors.toList());
    var historyVariables = historyVariableInstanceClient
        .getList(HistoryVariableInstanceQueryDto.builder()
            .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
            .processInstanceIdIn(processInstanceIds)
            .build());
    var processInstanceIdVariablesMap = historyVariables.stream()
        .collect(Collectors.groupingBy(HistoricVariableInstanceDto::getProcessInstanceId,
            Collectors.groupingBy(HistoricVariableInstanceDto::getName)));

    processInstances.forEach(pi -> {
      var variableNameVariableInstanceMap = processInstanceIdVariablesMap
          .getOrDefault(pi.getId(), Collections.emptyMap());

      fillExcerptId(pi, variableNameVariableInstanceMap);
      fillStatusTitle(pi, variableNameVariableInstanceMap);
    });
  }

  private void fillExcerptId(HistoryProcessInstance processInstance,
      Map<String, List<HistoricVariableInstanceDto>> variablesMap) {
    if (variablesMap.containsKey(Constants.SYS_VAR_PROCESS_EXCERPT_ID)) {
      var excerptId = variablesMap.get(Constants.SYS_VAR_PROCESS_EXCERPT_ID).get(0);
      processInstance.setExcerptId((String) excerptId.getValue());
    }
  }

  private void fillStatusTitle(HistoryProcessInstance processInstance,
      Map<String, List<HistoricVariableInstanceDto>> variablesMap) {
    var status = processInstance.getStatus();
    var code = status.getCode();
    var completionResultVariable = variablesMap.getOrDefault(
        Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, Collections.emptyList());
    status.setTitle(buildTitle(code, completionResultVariable));
  }
}
