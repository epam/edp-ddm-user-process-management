package com.epam.digital.data.platform.usrprcssmgt.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The class represents a data transfer object for finished process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HistoryUserProcessInstance {
  private String id;
  private String processDefinitionId;
  private String processDefinitionName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private HistoryStatusModel status;
  private String excerptId;
}