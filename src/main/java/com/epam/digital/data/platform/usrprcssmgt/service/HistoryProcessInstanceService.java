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
import com.epam.digital.data.platform.usrprcssmgt.api.HistoryProcessInstanceApi;
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
 * Base implementation of {@link HistoryProcessInstanceApi}. A proxy to the {@link
 * ProcessInstanceHistoryRestClient} that also maps the camunda response to the needed format and
 * localizes it if needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryProcessInstanceService implements HistoryProcessInstanceApi {

  private final ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;
  private final HistoryVariableInstanceClient historyVariableInstanceClient;

  private final ProcessInstanceMapper processInstanceMapper;

  private final MessageResolver messageResolver;

  @Override
  public List<HistoryProcessInstance> getHistoryProcessInstances(Pageable page) {
    log.info("Getting finished process instances. Parameters: {}", page);

    var historyProcessInstances = getCamundaProcessInstances(page);
    log.trace("Found {} Camunda process instances", historyProcessInstances.size());

    var result = mapToHistoryProcessInstances(historyProcessInstances);
    log.trace("Process instances filled with addition info - {}", result);

    log.info("{} process instances are found", result.size());
    return result;
  }

  @Override
  public HistoryProcessInstance getHistoryProcessInstanceById(String processInstanceId) {
    log.info("Get finished process instance by id {}", processInstanceId);

    var processInstance = processInstanceHistoryRestClient.getProcessInstanceById(
        processInstanceId);
    log.trace("Found Camunda process instance");

    var result = mapToHistoryProcessInstance(processInstance);
    log.trace("Process instance filled with addition info - {}", processInstance);

    return result;
  }

  @Override
  public CountResultDto getCountProcessInstances() {
    log.info("Getting count of finished process instances");

    var queryDto = HistoryProcessInstanceCountQueryDto.builder()
        .finished(true)
        .rootProcessInstances(true)
        .build();
    var result = processInstanceHistoryRestClient.getProcessInstancesCount(queryDto);

    log.info("Count of finished process instances is found - {}", result.getCount());
    return result;
  }

  private List<HistoricProcessInstanceDto> getCamundaProcessInstances(Pageable page) {
    var historyProcessInstanceQueryDto = HistoryProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .finished(true)
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();
    return processInstanceHistoryRestClient.getProcessInstances(historyProcessInstanceQueryDto);
  }

  /**
   * Fills process instance objects with addition fields such as excerptId and process instance
   * status
   */
  private List<HistoryProcessInstance> mapToHistoryProcessInstances(
      List<HistoricProcessInstanceDto> historyProcessInstances) {
    var processInstanceIds = extractProcessInstanceIds(historyProcessInstances);
    var groupedProcessInstanceVariables = getGroupedProcessInstanceVariables(processInstanceIds);

    return historyProcessInstances.stream()
        .map(hpi -> mapToHistoryProcessInstance(hpi,
            groupedProcessInstanceVariables.getOrDefault(hpi.getId(), List.of())))
        .collect(Collectors.toList());
  }

  private List<String> extractProcessInstanceIds(
      List<HistoricProcessInstanceDto> processInstances) {
    return processInstances.stream()
        .map(HistoricProcessInstanceDto::getId)
        .collect(Collectors.toList());
  }

  private Map<String, List<HistoricVariableInstanceDto>> getGroupedProcessInstanceVariables(
      List<String> processInstanceIds) {
    log.debug("Selecting all variables with prefix {} for processInstances {}",
        Constants.SYS_VAR_PREFIX_LIKE, processInstanceIds);
    var queryDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(processInstanceIds)
        .build();

    return historyVariableInstanceClient
        .getList(queryDto).stream()
        .collect(Collectors.groupingBy(HistoricVariableInstanceDto::getProcessInstanceId));
  }

  /**
   * Fills process instance object with addition fields such as excerptId and process instance
   * status
   */
  private HistoryProcessInstance mapToHistoryProcessInstance(
      HistoricProcessInstanceDto processInstance) {
    var processInstanceVariables = getProcessInstanceVariables(processInstance.getId());
    return mapToHistoryProcessInstance(processInstance, processInstanceVariables);
  }

  private List<HistoricVariableInstanceDto> getProcessInstanceVariables(String processInstanceId) {
    log.debug("Selecting all variables with prefix {} for processInstance {}",
        Constants.SYS_VAR_PREFIX_LIKE, processInstanceId);
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
