/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.integration.formprovider.dto.FormDataValidationDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.validation.service.FormValidationService;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.usrprcssmgt.config.properties.BpGroupConfigurationProperties;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.ProcessDefinitionGroup;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GroupedProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.ProcessDefinitionRemoteService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * A service that contains methods for working with a process definitions.
 * <p>
 * Implements such business functions:
 * <li>{@link ProcessDefinitionService#getProcessDefinitionByKey(String) Getting process definition
 * by process definition key}</li>
 * <li>{@link ProcessDefinitionService#getProcessDefinitions(GetProcessDefinitionsParams) Getting
 * list of process definition by params}</li>
 * <li>{@link ProcessDefinitionService#countProcessDefinitions(GetProcessDefinitionsParams) Getting
 * count of process definition by params}</li>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService {

  private final ProcessDefinitionRemoteService processDefinitionRemoteService;
  private final FormDataStorageService formDataStorageService;
  private final FormValidationService formValidationService;
  private final BpGroupConfigurationProperties bpGroupConfigurationProperties;

  /**
   * Getting process definition by process definition key
   *
   * @param key the process definition key
   * @return process definition entity
   */
  public ProcessDefinitionResponse getProcessDefinitionByKey(String key) {
    log.info("Getting process definition by key - {}", key);

    var result = processDefinitionRemoteService.getProcessDefinitionByKey(key);
    log.trace("Found process definition - {}", result);

    log.info("Process definition with key {} is found", key);
    return result;
  }

  /**
   * Getting process definition list by {@link GetProcessDefinitionsParams parameters}
   *
   * @param params the process definition query parameters
   * @return list of process definition entities
   */
  public List<ProcessDefinitionResponse> getProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting list of process definitions. Params: {}", params);

    var result = processDefinitionRemoteService.getProcessDefinitions(params);

    log.info("List of process definitions is found. Size - {}", result.size());
    return result;
  }

  /**
   * Getting process definition count by {@link GetProcessDefinitionsParams parameters}
   *
   * @param params the process definition query parameters
   * @return count of process definition entities
   */
  public CountResponse countProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting count of process definitions. Params: {}", params);

    var result = processDefinitionRemoteService.countProcessDefinitions(params);

    log.info("Count of process definitions is found - {}", result.getCount());
    return result;
  }

  /**
   * Starting process instance by process definition key
   *
   * @param key the process definition key
   * @return started process instance entity
   */
  public StartProcessInstanceResponse startProcessInstance(String key, Authentication authentication) {
    log.info("Starting process instance for definition with key {}", key);
    FormDataDto form = new FormDataDto();
    return startProcess(key, form, authentication);
  }

  /**
   * Starting process instance by process definition key with start form data
   * <p>
   * Performs:
   * <ol>
   *   <li>Checks if process definition by key exists</li>
   *   <li>Validates the start form data</li>
   *   <li>Save the start form data in form data storage</li>
   *   <li>Starts process instance itself</li>
   * </ol>
   *
   * @param key         the process definition key
   * @param formDataDto the start form data dto
   * @return started process instance entity
   * @throws StartFormException  if start form hasn't defined in business process
   * @throws ValidationException if form data hasn't pass the validation
   */
  public StartProcessInstanceResponse startProcessInstanceWithForm(String key,
      FormDataDto formDataDto, Authentication authentication) {
    log.info("Starting process instance with start form for definition with key {}", key);

    var processDefinition = processDefinitionRemoteService.getProcessDefinitionByKey(key);
    var startFormKey = getStartFormKey(processDefinition);
    log.trace("Found process definition with key - {} and formKey - {}. Id - {}",
        key, startFormKey, processDefinition.getId());

    validateFormData(processDefinition, formDataDto);
    log.trace("Process definition form data is valid. Id - {}", processDefinition.getId());

    return startProcess(key, formDataDto, authentication);
  }

  private StartProcessInstanceResponse startProcess(String key, FormDataDto formDataDto,
      Authentication authentication) {
    formDataDto.setAccessToken((String) authentication.getCredentials());
    var uuid = UUID.randomUUID().toString();
    var formDataKey = formDataStorageService.putStartFormData(key, uuid, formDataDto);
    log.trace("Process definition form data was saved. Process definition key - {}", key);

    try {
      var result = processDefinitionRemoteService.startProcessInstance(key, formDataKey);

      log.info("Starting process instance of process definition {} finished. "
          + "Process instance id {}", key, result.getId());
      return result;
    } catch (Exception exception) {
      formDataStorageService.delete(Set.of(formDataKey));
      throw exception;
    }
  }

  public GroupedProcessDefinitionResponse getGroupedProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting grouped process definitions. Params: {}", params);
    var processDefinitions =
        processDefinitionRemoteService.getProcessDefinitions(params).stream()
            .collect(Collectors.toMap(ProcessDefinitionResponse::getKey, Function.identity()));

    // getting the processes defined in the file and removing them from the map
    var groupsResponse = getGroupedProcessDefinition(processDefinitions);
    var ungroupedResponse = getUngroupedProcessDefinition(processDefinitions);

    if (!processDefinitions.isEmpty()) {
      var processesNotInTheGroupingFile = processDefinitions.values().stream()
          .sorted(Comparator.comparing(ProcessDefinitionResponse::getName))
          .collect(Collectors.toList());
      ungroupedResponse.addAll(processesNotInTheGroupingFile);
    }
    log.info("Process definitions are found. Group count - {}, ungrouped processes - {}", groupsResponse.size(), ungroupedResponse.size());
    return GroupedProcessDefinitionResponse.builder()
        .groups(groupsResponse)
        .ungrouped(ungroupedResponse)
        .build();
  }

  private List<ProcessDefinitionResponse> getUngroupedProcessDefinition(Map<String, ProcessDefinitionResponse> processDefinitions) {
    var ungrouped = bpGroupConfigurationProperties.getUngrouped();
    if (Objects.isNull(ungrouped)) {
      return Lists.newArrayList();
    }
    return getProcessDefinitionsDefinedInFile(ungrouped, processDefinitions);
  }

  private List<ProcessDefinitionGroup> getGroupedProcessDefinition(Map<String, ProcessDefinitionResponse> processDefinitions) {
    var groups = bpGroupConfigurationProperties.getGroups();
    if (Objects.isNull(groups)) {
      return Lists.newArrayList();
    }
    var groupsResponse = new ArrayList<ProcessDefinitionGroup>();
    groups.forEach(groupedProcessDefinition -> {
      var definitionKeys = Optional.ofNullable(groupedProcessDefinition.getProcessDefinitions())
              .orElse(Lists.newArrayList());
      var groupedResponse = getProcessDefinitionsDefinedInFile(definitionKeys, processDefinitions);
      if (!groupedResponse.isEmpty()) {
        var processDefinitionGroup = new ProcessDefinitionGroup();
        processDefinitionGroup.setName(groupedProcessDefinition.getName());
        processDefinitionGroup.setProcessDefinitions(groupedResponse);
        groupsResponse.add(processDefinitionGroup);
      }
    });

    return groupsResponse;
  }

  private List<ProcessDefinitionResponse> getProcessDefinitionsDefinedInFile(
      List<String> processDefinitionKeys, Map<String, ProcessDefinitionResponse> processDefinitions) {
    var result = new ArrayList<ProcessDefinitionResponse>();
    processDefinitionKeys.forEach(key -> {
      var processDefinition = processDefinitions.get(key);
      if (Objects.nonNull(processDefinition)) {
        result.add(processDefinition);
        processDefinitions.remove(key);
      }
    });
    return result;
  }

  private String getStartFormKey(ProcessDefinitionResponse processDefinition) {
    var startFormKey = processDefinition.getFormKey();
    if (Objects.nonNull(startFormKey)) {
      return startFormKey;
    }
    log.warn("Start form key for process definition {} not found!", processDefinition.getKey());
    throw new StartFormException("Start form does not exist!");
  }

  private void validateFormData(ProcessDefinitionResponse processDefinition, FormDataDto formDataDto) {
    log.debug("Start validation of start formData");
    var formValidationDto =
        FormDataValidationDto.builder()
            .data(formDataDto.getData())
            .processInstanceId(processDefinition.getId())
            .build();
    var formValidationResponseDto = formValidationService.validateForm(getStartFormKey(processDefinition),
        formValidationDto);
    if (!formValidationResponseDto.isValid()) {
      log.warn("Start form data did not pass validation, form key: {}", processDefinition.getFormKey());
      throw new ValidationException(formValidationResponseDto.getError());
    }
    log.debug("FormData passed the validation");
  }
}
