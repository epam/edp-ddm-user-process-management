package com.epam.digital.data.platform.usrprcssmgt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class represents a data transfer object for process instance, as result of started process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartProcessInstanceResponse {
  private String id;
  private String processDefinitionId;
  private boolean ended;
}
