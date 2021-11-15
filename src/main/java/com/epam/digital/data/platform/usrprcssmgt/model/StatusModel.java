package com.epam.digital.data.platform.usrprcssmgt.model;

import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The class defines a status for unfinished process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatusModel {

  private ProcessInstanceStatus code;
  private String title;
}
