package com.epam.digital.data.platform.usrprcssmgt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class represents a data transfer object for process definition instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProcessDefinitionDto {

  private String id;
  private String key;
  private String name;
  private boolean suspended;
  private String formKey;
}
