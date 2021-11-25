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

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;

/**
 * Base service that is responsible for saving form data in ceph
 */
public interface FormDataRemoteService {

  /**
   * Save form data in form data storage
   *
   * @param processDefinitionKey key of the process definition form data is saved for
   * @param formDataDto          the form data itself
   * @return form data storage (ceph) key of the saved form data
   */
  String saveStartFormData(String processDefinitionKey, FormDataDto formDataDto);
}
