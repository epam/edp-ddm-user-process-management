package com.epam.digital.data.platform.usrprcssmgt.model;

import lombok.Data;

/**
 * The class defines an active and non-suspended process definition instance.
 */
@Data
public class GetProcessDefinitionsParams {
  private boolean active = true;
  private boolean suspended = false;
}
