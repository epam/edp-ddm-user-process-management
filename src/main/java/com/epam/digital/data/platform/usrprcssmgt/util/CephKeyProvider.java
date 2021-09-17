package com.epam.digital.data.platform.usrprcssmgt.util;

import org.springframework.stereotype.Component;

/**
 * The class represents a provider that is used to generate the key to get the ceph document
 */
@Component
public class CephKeyProvider {

  private static final String START_FORM_DATA_CEPH_KEY_FORMAT = "process-definition/%s/start-form/%s";

  /**
   * Method for generating the ceph key to save start form data, uses process definition key and
   * generated unique identifier to construct the key
   *
   * @param processDefinitionKey process definition key
   * @param uuid                 unique identifier
   * @return generated ceph key
   */
  public String generateStartFormKey(String processDefinitionKey, String uuid) {
    return String.format(START_FORM_DATA_CEPH_KEY_FORMAT, processDefinitionKey, uuid);
  }
}
