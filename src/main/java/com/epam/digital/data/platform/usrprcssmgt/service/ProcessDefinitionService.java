package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.bpms.client.StartFormRestClient;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessDefinitionApi;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessDefinitionApi}. A proxy to the {@link
 * ProcessDefinitionRestClient} that also maps the camunda response to the needed format.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService implements ProcessDefinitionApi {

  private final ProcessDefinitionRestClient processDefinitionRestClient;
  private final StartFormRestClient startFormRestClient;

  private final ProcessInstanceMapper processInstanceMapper;
  private final ProcessDefinitionMapper processDefinitionMapper;

  @Override
  public UserProcessDefinitionDto getProcessDefinitionByKey(String key) {
    log.info("Getting process definition by key - {}", key);

    var userProcessDefinitionDto = getUserProcessDefinitionByKey(key);
    log.trace("Found process definition - {}", userProcessDefinitionDto);

    fillProcessDefinitionFormKey(List.of(userProcessDefinitionDto));
    log.trace("Process definition filled - {}", userProcessDefinitionDto);

    log.info("Process definition with key {} is found", key);
    return userProcessDefinitionDto;
  }

  @Override
  public List<UserProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting list of process definitions. Params: {}", params);

    var userProcessDefinitionDtos = getUserProcessDefinitionDtos(params);
    log.trace("Found process definitions - {}", userProcessDefinitionDtos);

    fillProcessDefinitionFormKey(userProcessDefinitionDtos);
    log.trace("Filled process definitions - {}", userProcessDefinitionDtos);

    log.info("List of process definitions is found. Size - {}", userProcessDefinitionDtos.size());
    return userProcessDefinitionDtos;
  }

  @Override
  public CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting count of process definitions. Params: {}", params);

    var queryDto = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .build();
    var result = processDefinitionRestClient.getProcessDefinitionsCount(queryDto);

    log.info("Count of process definitions is found - {}", result.getCount());
    return result;
  }

  /**
   * Start process instance for process definition by process-definition id
   *
   * @param key process definition key
   * @return new process-instance object
   */
  public StartProcessInstanceResponse startProcessDefinition(String key) {
    return startProcessInstance(key, new StartProcessInstanceDto());
  }

  /**
   * Start process instance for process definition by process-definition id with form data key
   *
   * @param key         process definition key
   * @param formDataKey key of the actual form data that is stored in some storage
   * @return new process-instance object
   */
  public StartProcessInstanceResponse startProcessDefinition(String key, String formDataKey) {
    var startProcessInstanceDto = prepareStartProcessInstance(formDataKey);

    log.trace("Starting instance of process definition. Key - {}", key);
    return startProcessInstance(key, startProcessInstanceDto);
  }

  private UserProcessDefinitionDto getUserProcessDefinitionByKey(String key) {
    var camundaProcessDefinitionDto = processDefinitionRestClient.getProcessDefinitionByKey(key);
    return processDefinitionMapper.toUserProcessDefinitionDto(camundaProcessDefinitionDto);
  }

  private List<UserProcessDefinitionDto> getUserProcessDefinitionDtos(
      GetProcessDefinitionsParams params) {
    var queryDto = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue())
        .build();
    var processDefinitionDtos = processDefinitionRestClient.getProcessDefinitionsByParams(queryDto);
    return processDefinitionMapper.toUserProcessDefinitionDtos(processDefinitionDtos);
  }

  private void fillProcessDefinitionFormKey(List<UserProcessDefinitionDto> processDefinitionDtos) {
    var processDefinitionIds = processDefinitionDtos.stream()
        .map(UserProcessDefinitionDto::getId)
        .collect(Collectors.toSet());
    log.debug("Selecting and filling form keys to process definition list. Ids - {}",
        processDefinitionIds);

    var startFormKeyMap = startFormRestClient.getStartFormKeyMap(
        StartFormQueryDto.builder().processDefinitionIdIn(processDefinitionIds).build());

    processDefinitionDtos.forEach(pd -> {
      var formKey = startFormKeyMap.get(pd.getId());
      if (Objects.nonNull(formKey)) {
        log.trace("start form {} defined for process definition {}", formKey, pd.getId());
        pd.setFormKey(formKey);
      }
    });
    log.debug("Process definition start forms founded");
  }

  private StartProcessInstanceResponse startProcessInstance(String key,
      StartProcessInstanceDto startProcessInstanceDto) {
    var processInstanceDto = processDefinitionRestClient.startProcessInstanceByKey(key,
        startProcessInstanceDto);
    log.trace("Process instance started. Process instanceId - {}", processInstanceDto.getId());

    return processInstanceMapper.toStartProcessInstanceResponse(processInstanceDto);
  }

  private StartProcessInstanceDto prepareStartProcessInstance(String startFormKey) {
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(startFormKey);

    var variables =
        Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, variableValueDto);

    var startProcessInstanceDto = new StartProcessInstanceDto();
    startProcessInstanceDto.setVariables(variables);
    return startProcessInstanceDto;
  }
}
