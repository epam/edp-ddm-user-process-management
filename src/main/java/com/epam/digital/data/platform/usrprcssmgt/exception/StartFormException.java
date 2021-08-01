package com.epam.digital.data.platform.usrprcssmgt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception that is thrown when start form for process definition does not exist
 */
@Getter
@RequiredArgsConstructor
public class StartFormException extends RuntimeException{

  private final String message;
}
