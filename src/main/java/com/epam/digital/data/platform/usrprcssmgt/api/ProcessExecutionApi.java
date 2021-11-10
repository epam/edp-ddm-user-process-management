package com.epam.digital.data.platform.usrprcssmgt.api;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;

/**
 * The {@link ProcessExecutionApi} class represents a service with operations of a business process
 * starting
 * <p>
 * Provides such methods as:
 * <li>{@link ProcessExecutionApi#startProcessDefinition(String)} to start a process instance of a
 * process definition by id </li>
 * <li>{@link ProcessExecutionApi#startProcessDefinitionWithForm(String, FormDataDto)} to start a
 * process instance of a process definition by id with start form</li>
 */
public interface ProcessExecutionApi {

  /**
   * Method for running process instance by process definition id, returns started process instance
   * entity.
   *
   * @param key process definition key
   * @return an entity that defines the started process instance
   */
  StartProcessInstanceResponse startProcessDefinition(String key);

  /**
   * Method for running process instance by process definition id with start form, returns started
   * process instance entity.
   *
   * @param key         process definition key
   * @param formDataDto start from data
   * @return an entity that defines the started process instance
   * @throws StartFormException  if there's not defined start form key for process definition
   * @throws ValidationException if form data hasn't passed the validation
   */
  StartProcessInstanceResponse startProcessDefinitionWithForm(String key, FormDataDto formDataDto);
}
