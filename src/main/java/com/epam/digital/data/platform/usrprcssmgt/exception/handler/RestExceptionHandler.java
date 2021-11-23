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
    log.error("Start form does not exist", ex);
    return new ResponseEntity<>(systemErrorDto, HttpStatus.BAD_REQUEST);
  }
}
