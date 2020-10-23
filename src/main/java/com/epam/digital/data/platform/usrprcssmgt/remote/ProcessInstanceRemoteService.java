/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.usrprcssmgt.remote;

import com.epam.digital.data.platform.starter.security.SystemRole;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import java.util.List;

/**
 * Service for {@link GetProcessInstanceResponse} entity. Contains methods for accessing finished
 * process instances.
 */
public interface ProcessInstanceRemoteService {

  /**
   * Method for getting the number of unfinished process instances with root process instance
   *
   * @return an entity that defines the number of unfinished process instances.
   */
  CountResponse countProcessInstances();

  /**
   * Method for getting a list of unfinished process instances. The list must be sorted by start
   * time and in ascending order.
   *
   * @param page       defines the pagination parameters to shrink result lust
   * @param systemRole current user role
   * @return a list of unfinished process instances.
   */
  List<GetProcessInstanceResponse> getProcessInstances(Pageable page, SystemRole systemRole);
}
