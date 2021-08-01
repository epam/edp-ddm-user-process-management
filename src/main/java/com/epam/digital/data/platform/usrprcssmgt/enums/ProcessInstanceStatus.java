package com.epam.digital.data.platform.usrprcssmgt.enums;

import com.epam.digital.data.platform.starter.localization.MessageTitle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a list of possible statuses in a business process instance. The process can change
 * status at runtime.
 */
@Getter
@RequiredArgsConstructor
public enum ProcessInstanceStatus implements MessageTitle {
  PENDING("process-instance.status.title.pending"),
  SUSPENDED("process-instance.status.title.suspended"),
  IN_PROGRESS("process-instance.status.title.in-progress"),
  COMPLETED("process-instance.status.title.completed"),
  EXTERNALLY_TERMINATED("process-instance.status.title.externally-terminated"),

  CITIZEN_PENDING("process-instance.status.title.citizen-pending"),
  CITIZEN_SUSPENDED("process-instance.status.title.citizen-suspended"),
  CITIZEN_IN_PROGRESS("process-instance.status.title.citizen-in-progress");

  private final String titleKey;
}
