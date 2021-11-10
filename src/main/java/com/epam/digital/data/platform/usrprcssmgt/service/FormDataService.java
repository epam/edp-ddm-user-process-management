package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.usrprcssmgt.util.CephKeyProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base service that is responsible for saving form data in ceph
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDataService {

  private final CephKeyProvider cephKeyProvider;
  private final FormDataCephService cephService;

  /**
   * Save form data in form data storage (ceph)
   *
   * @param processDefinitionKey key of the process definition form data is saved for
   * @param formDataDto          the form data itself
   * @return form data storage (ceph) key of the saved form data
   */
  public String saveStartFormData(String processDefinitionKey, FormDataDto formDataDto) {
    var cephKey = generateStartFormKey(processDefinitionKey);
    putStringFormDataToCeph(cephKey, formDataDto);
    return cephKey;
  }

  private String generateStartFormKey(String processDefinitionKey) {
    var uuid = UUID.randomUUID().toString();
    return cephKeyProvider.generateStartFormKey(processDefinitionKey, uuid);
  }

  private void putStringFormDataToCeph(String startFormKey, FormDataDto formData) {
    try {
      log.debug("Put start form to ceph. Key - {}", startFormKey);
      cephService.putFormData(startFormKey, formData);
      log.debug("Start form data is put to ceph. Key - {}", startFormKey);
    } catch (CephCommunicationException ex) {
      log.warn("Couldn't put form data to ceph with key - {}", startFormKey, ex);
      throw ex;
    }
  }
}
