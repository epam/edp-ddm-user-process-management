package com.epam.digital.data.platform.usrprcssmgt.api;

import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * The {@link ProcessInstanceApi} class represents a facade with operations on  {@link
 * ProcessInstance} entity. It contains methods for working with unfinished process instances.
 * <p>
 * Provides such methods as:
 * <li>{@link ProcessInstanceApi#getOfficerProcessInstances(Pageable)} to get a pageable list of
 * running officer process instances</li>
 * <li>{@link ProcessInstanceApi#getCitizenProcessInstances(Pageable)} to get a pageable list of
 * running citizen process instances</li>
 * <li>{@link ProcessInstanceApi#countProcessInstances()} to get a running process instance
 * count</li>
 */
public interface ProcessInstanceApi {

  /**
   * Method for getting the number of unfinished process instances with root process instance
   *
   * @return an entity that defines the number of unfinished process instances.
   */
  CountResultDto countProcessInstances();

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the role of an officer.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list of unfinished process instances.
   */
  List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page);

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order. Performed by a user with the citizen role.
   *
   * @param page defines the pagination parameters to shrink result lust
   * @return a list of unfinished process instances.
   */
  List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page);
}
