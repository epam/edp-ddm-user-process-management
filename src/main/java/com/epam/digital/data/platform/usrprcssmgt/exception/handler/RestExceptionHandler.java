package com.epam.digital.data.platform.usrprcssmgt.exception.handler;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.exception.StartFormException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * The class represents a handler for exception. Contains a method to handle {@link
 * StartFormException} exception.
 */
@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

  /**
   * Catching {@link StartFormException} exception and return response about start form
   * does not exist
   *
   * @param ex caught exception
   * @return response entity with error
   */
  @ExceptionHandler(StartFormException.class)
  public ResponseEntity<SystemErrorDto> handleStartFormException(StartFormException ex) {

    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .message(ex.getMessage())
        .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
        .localizedMessage(null)
        .build();

    return new ResponseEntity<>(systemErrorDto, HttpStatus.BAD_REQUEST);
  }

}
