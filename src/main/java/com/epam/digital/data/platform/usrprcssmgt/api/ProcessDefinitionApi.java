package com.epam.digital.data.platform.usrprcssmgt.api;

import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

/**
 * The {@link ProcessDefinitionApi} class represents a facade with operations on  {@link
 * org.camunda.bpm.engine.repository.ProcessDefinition} entity.
 * <p>
 * Provides such methods as:
 * <li>{@link ProcessDefinitionApi#getProcessDefinitionByKey(String)} to get a process definition
 * by key</li>
 * <li>{@link ProcessDefinitionApi#getProcessDefinitions(GetProcessDefinitionsParams)} to get a
 * list of process definitions by params that is defined in {@link GetProcessDefinitionsParams}</li>
 * <li>{@link ProcessDefinitionApi#countProcessDefinitions(GetProcessDefinitionsParams)} to get a
 * count of process definitions by params that is defined in {@link GetProcessDefinitionsParams}</li>
 */
public interface ProcessDefinitionApi {

  /**
   * Method for getting the process definition entity by process definition key.
   *
   * @param key process definition key
   * @return process definition entity
   */
  UserProcessDefinitionDto getProcessDefinitionByKey(String key);

  /**
   * Method for getting a list of the latest version of  process definitions entities. The list must
   * be sorted by process definition name and in ascending order.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return a list of process definitions.
   */
  List<UserProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params);

  /**
   * Method for getting thr number of process definitions by parameters.
   *
   * @param params entity that defines an active and non-suspended process definition.
   * @return an entity that defines the number of process definitions.
   */
  CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params);
}
