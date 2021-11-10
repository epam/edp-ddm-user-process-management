package com.epam.digital.data.platform.usrprcssmgt.service;

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
import com.epam.digital.data.platform.usrprcssmgt.util.CephKeyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormDataServiceTest {

  @InjectMocks
  private FormDataService formDataService;
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
