package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.bpms.client.StartFormRestClient;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessDefinitionMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import com.epam.digital.data.platform.usrprcssmgt.util.CephKeyProvider;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The class represents a service for {@link ProcessDefinitionDto} entity and contains methods for
 * working with a process definition instance.
 * <p>
 * The ProcessDefinitionService class provides a method to get a list of process definitions
 * <p>
 * The ProcessDefinitionService class provides a method to get the number of process definitions
 * <p>
 * The ProcessDefinitionService class provides a method to start a process definitions
 */
@Service
@Slf4j
public class ProcessDefinitionService {

  private static final String START_FORM_CEPH_KEY = "start_form_ceph_key";

  @Autowired
  private ProcessDefinitionRestClient processDefinitionRestClient;
  @Autowired
  private StartFormRestClient startFormRestClient;
  @Autowired
  private ProcessInstanceMapper processInstanceMapper;
  @Autowired
  private ProcessDefinitionMapper processDefinitionMapper;
  @Autowired
  private CephKeyProvider cephKeyProvider;
  @Autowired
  private FormDataCephService cephService;
  @Autowired
  private FormValidationService formValidationService;

  /**
   * Method for getting the process definition entity by id.
   *
   * @param id process definition identifier
   * @return process definition entity
   */
  public UserProcessDefinitionDto getProcessDefinitionById(String id) {
    log.info("Getting process definition by id - {}", id);

    var userProcessDefinitionDto = processDefinitionMapper
        .toUserProcessDefinitionDto(processDefinitionRestClient.getProcessDefinition(id));
    log.trace("Found process definition - {}", userProcessDefinitionDto);

    fillProcessDefinitionFormKey(List.of(userProcessDefinitionDto), List.of(id));
    log.trace("Process definition filled - {}", userProcessDefinitionDto);

    log.info("Process definition with id {} is found", id);
    return userProcessDefinitionDto;
  }

  /**
   * Method for getting thr number of process definitions by parameters.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return an entity that defines the number of process definitions.
   */
  public CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting count of process definitions. Params: {}", params);

    var countDto = processDefinitionRestClient.getProcessDefinitionsCount(
        ProcessDefinitionQueryDto.builder().latestVersion(true)
            .active(params.isActive())
            .suspended(params.isSuspended())
            .build());

    log.info("Count of process definitions is found - {}", countDto.getCount());
    return countDto;
  }

  /**
   * Method for getting a list of the latest version of  process definitions entities. The list must
   * be sorted by process definition name and in ascending order.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return a list of process definitions.
   */
  public List<UserProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting list of process definitions. Params: {}", params);

    var processDefinitionDtos = processDefinitionRestClient.getProcessDefinitionsByParams(
        ProcessDefinitionQueryDto.builder().latestVersion(true)
            .active(params.isActive())
            .suspended(params.isSuspended())
            .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
            .sortOrder(SortOrder.ASC.stringValue()).build());
    var userProcessDefinitionDtos = processDefinitionMapper
        .toUserProcessDefinitionDtos(processDefinitionDtos);
    log.trace("Found process definitions - {}", userProcessDefinitionDtos);

    var processDefinitionIds = userProcessDefinitionDtos.stream()
        .map(UserProcessDefinitionDto::getId)
        .collect(Collectors.toList());
    fillProcessDefinitionFormKey(userProcessDefinitionDtos, processDefinitionIds);
    log.trace("Filled process definitions - {}", userProcessDefinitionDtos);

    log.info("List of process definitions is found. Size - {}, ids - {}", processDefinitionDtos.size(),
        processDefinitionIds);
    return userProcessDefinitionDtos;
  }

  /**
   * Method for running process instance by process definition id, returns started process instance
   * entity.
   *
   * @param id process definition identifier
   * @return an entity that defines the started process instance
   */
  public StartProcessInstanceResponse startProcessDefinition(String id) {
    log.info("Starting process instance for definition with id {}", id);

    var key = processDefinitionRestClient.getProcessDefinition(id).getKey();
    log.trace("Process definition key found - {}", key);

    var response = processInstanceMapper.toStartProcessInstanceResponse(
        processDefinitionRestClient.startProcessInstance(id, new StartProcessInstanceDto()));

    log.info("Process instance for process definition {} started. Process instance id {}", id, response.getId());
    return response;
  }

  /**
   * Method for running process instance by process definition id with start form, returns started
   * process instance entity.
   *
   * @param id          process definition identifier
   * @param formDataDto start from data
   * @return an entity that defines the started process instance
   */
  public StartProcessInstanceResponse startProcessDefinitionWithForm(String id,
      FormDataDto formDataDto) {
    log.info("Starting process instance for definition with id {}", id);
    log.trace("Input form data dto - {}", formDataDto);

    var processDefinition = processDefinitionRestClient.getProcessDefinition(id);
    var processDefinitionKey = processDefinition.getKey();

    log.trace("Validating start form");
    var startForm = processDefinitionRestClient.getStartForm(id);
    checkProcessDefinitionStartForm(startForm, id);
    validateFormData(startForm.getKey(), formDataDto);
    log.trace("Put start form data to ceph");
    var uuid = UUID.randomUUID().toString();
    var startFormKey = cephKeyProvider.generateStartFormKey(processDefinitionKey, uuid);
    putStringFormDataToCeph(startFormKey, formDataDto);
    var startProcessInstanceDto = prepareStartProcessInstance(startFormKey);

    log.trace("Starting instance of process definition {} with id - {}", processDefinitionKey, id);
    var response = processInstanceMapper.toStartProcessInstanceResponse(
        processDefinitionRestClient.startProcessInstance(id, startProcessInstanceDto));

    log.info("Starting process instance of process definition {} with id - {} finished", processDefinitionKey, id);
    return response;
  }

  private void fillProcessDefinitionFormKey(List<UserProcessDefinitionDto> processDefinitionDtos,
                                            List<String> processDefinitionIds) {
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

  private void checkProcessDefinitionStartForm(FormDto startForm, String id) {
    if (Objects.isNull(startForm) || Objects.isNull(startForm.getKey())) {
      log.error("Start form does not exist for process definition with id: {}", id);
      throw new StartFormException("Start form does not exist!");
    }
  }

  private StartProcessInstanceDto prepareStartProcessInstance(String startFormKey) {
    var startProcessInstanceDto = new StartProcessInstanceDto();
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(startFormKey);
    startProcessInstanceDto.setVariables(Map.of(START_FORM_CEPH_KEY, variableValueDto));
    return startProcessInstanceDto;
  }

  private void putStringFormDataToCeph(String startFormKey, FormDataDto formData) {
    try {
      log.debug("Put start form to ceph. Key - {}, value - {}", startFormKey, formData);
      cephService.putFormData(startFormKey, formData);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't put form data to ceph", ex);
      throw ex;
    }
    log.debug("Start form data is put to ceph");
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    log.debug("Start validation of start formData {}", formDataDto);
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      log.error("Start form data did not pass validation, form key: {}", formId);
      throw new ValidationException(formValidationResponseDto.getError());
    }
    log.debug("FormData passed the validation");
  }
}
