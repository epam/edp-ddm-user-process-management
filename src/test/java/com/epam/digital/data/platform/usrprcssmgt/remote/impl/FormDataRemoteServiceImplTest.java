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

package com.epam.digital.data.platform.usrprcssmgt.remote.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormDataRemoteServiceImplTest {

  @InjectMocks
  private FormDataRemoteServiceImpl formDataService;
  @Mock
  private CephKeyProvider cephKeyProvider;
  @Mock
  private FormDataCephService formDataCephService;

  @Test
  void saveStartFormData() {
    var processDefinitionKey = "processDefinition";
    var formDataKey = "formDataKey";
    var formData = Mockito.mock(FormDataDto.class);
    when(cephKeyProvider.generateStartFormKey(eq(processDefinitionKey), anyString()))
        .thenReturn(formDataKey);

    var result = formDataService.saveStartFormData(processDefinitionKey, formData);

    assertThat(result).isSameAs(formDataKey);

    verify(formDataCephService).putFormData(formDataKey, formData);
  }

  @Test
  void saveStartFormData_cephCommunicationException() {
    var processDefinitionKey = "processDefinition";
    var formDataKey = "formDataKey";
    var formData = Mockito.mock(FormDataDto.class);
    when(cephKeyProvider.generateStartFormKey(eq(processDefinitionKey), anyString()))
        .thenReturn(formDataKey);
    var expectedException = new CephCommunicationException("ceph not working", new Exception());
    doThrow(expectedException).when(formDataCephService).putFormData(formDataKey, formData);

    var ex = assertThrows(CephCommunicationException.class,
        () -> formDataService.saveStartFormData(processDefinitionKey, formData));

    assertThat(ex).isSameAs(expectedException);

    verify(formDataCephService).putFormData(formDataKey, formData);
  }
}
