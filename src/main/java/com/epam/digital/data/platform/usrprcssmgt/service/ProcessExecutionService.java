package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessExecutionApi;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessExecutionApi}. Contains logic about starting business
 * process instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessExecutionService implements ProcessExecutionApi {

  private final ProcessDefinitionService processDefinitionService;
  private final FormDataService formDataService;
  private final FormValidationService formValidationService;

  @Override
  public StartProcessInstanceResponse startProcessDefinition(String key) {
    log.info("Starting process instance for definition with key {}", key);

    var result = processDefinitionService.startProcessDefinition(key);

    log.info("Process instance for process definition {} started. Process instance id {}", key,
        result.getId());
    return result;
  }

  @Override
  public StartProcessInstanceResponse startProcessDefinitionWithForm(String key,
      FormDataDto formDataDto) {
    log.info("Starting process instance with start form for definition with key {}", key);

    var processDefinition = processDefinitionService.getProcessDefinitionByKey(key);
    var startFormKey = getStartFormKey(processDefinition);
    log.trace("Found process definition with key - {} and formKey - {}. Id - {}",
        key, startFormKey, processDefinition.getId());

    validateFormData(startFormKey, formDataDto);
    log.trace("Process definition form data is valid. Id - {}", processDefinition.getId());

    var formDataKey = formDataService.saveStartFormData(key, formDataDto);
    log.trace("Process definition form data was saved. Id - {}", processDefinition.getId());

    var result = processDefinitionService.startProcessDefinition(key, formDataKey);

    log.info("Starting process instance of process definition {} with id - {} finished. "
        + "Process instance id {}", key, processDefinition.getId(), result.getId());
    return result;
  }

  private String getStartFormKey(UserProcessDefinitionDto processDefinition) {
    var startFormKey = processDefinition.getFormKey();
    if (Objects.nonNull(startFormKey)) {
      return startFormKey;
    }
    log.warn("Start form key for process definition {} not found!", processDefinition.getKey());
    throw new StartFormException("Start form does not exist!");
  }

  private void validateFormData(String formId, FormDataDto formDataDto) {
    log.debug("Start validation of start formData");
    var formValidationResponseDto = formValidationService.validateForm(formId, formDataDto);
    if (!formValidationResponseDto.isValid()) {
      log.warn("Start form data did not pass validation, form key: {}", formId);
      throw new ValidationException(formValidationResponseDto.getError());
    }
    log.debug("FormData passed the validation");
  }
}
