package com.epam.digital.data.platform.usrprcssmgt.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class represents a data transfer object for finished process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryProcessInstance {
  private String id;
  private String processDefinitionId;
  private String processDefinitionName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private HistoryStatusModel status;
}
