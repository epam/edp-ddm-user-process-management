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

package com.epam.digital.data.platform.usrprcssmgt.i18n;

import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.starter.localization.MessageTitle;
import com.epam.digital.data.platform.starter.security.SystemRole;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a list of possible statuses in a business process instance. The process can change
 * status at runtime.
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum ProcessInstanceStatusMessageTitle implements MessageTitle {
  PENDING(HistoryProcessInstanceStatus.PENDING, SystemRole.OFFICER,
      "process-instance.status.title.pending"),
  SUSPENDED(HistoryProcessInstanceStatus.SUSPENDED, SystemRole.OFFICER,
      "process-instance.status.title.suspended"),
  IN_PROGRESS(HistoryProcessInstanceStatus.ACTIVE, SystemRole.OFFICER,
      "process-instance.status.title.in-progress"),
  COMPLETED(HistoryProcessInstanceStatus.COMPLETED, "process-instance.status.title.completed"),
  EXTERNALLY_TERMINATED(HistoryProcessInstanceStatus.EXTERNALLY_TERMINATED,
      "process-instance.status.title.externally-terminated"),

  CITIZEN_PENDING(HistoryProcessInstanceStatus.PENDING, SystemRole.CITIZEN,
      "process-instance.status.title.citizen-pending"),
  CITIZEN_SUSPENDED(HistoryProcessInstanceStatus.SUSPENDED, SystemRole.CITIZEN,
      "process-instance.status.title.citizen-suspended"),
  CITIZEN_IN_PROGRESS(HistoryProcessInstanceStatus.ACTIVE, SystemRole.CITIZEN,
      "process-instance.status.title.citizen-in-progress");

  private final HistoryProcessInstanceStatus processInstanceStatus;
  private SystemRole systemRole;
  private final String titleKey;

  public static ProcessInstanceStatusMessageTitle from(
      HistoryProcessInstanceStatus processInstanceStatus, SystemRole systemRole) {

    return Stream.of(values())
        .filter(message -> message.getProcessInstanceStatus().equals(processInstanceStatus))
        .filter(message -> Objects.isNull(message.getSystemRole())
            || message.getSystemRole().equals(systemRole))
        .reduce((messageTitle, messageTitle2) -> {
          throw new IllegalStateException("More than 1 message found");
        })
        .orElse(null);
  }
}
