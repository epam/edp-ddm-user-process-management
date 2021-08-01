package com.epam.digital.data.platform.usrprcssmgt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class defines a status for finished process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryStatusModel {

  private String code;
  private String title;
}
