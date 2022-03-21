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

package com.epam.digital.data.platform.usrprcssmgt.controller;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorizeAnySystemRole
@RequestMapping("/api/process-definition")
public class ProcessDefinitionController {

  private final ProcessDefinitionService processDefinitionService;

  @GetMapping("/{key}")
  @Operation(
      summary = "Retrieve process definition by key",
      description = "Returns business process definition entity")
  public ProcessDefinitionResponse getProcessDefinitionByKey(@PathVariable("key") String key) {
    return processDefinitionService.getProcessDefinitionByKey(key);
  }

  @GetMapping
  @Operation(
      summary = "Retrieve all process definitions",
      description = "Returns business process definitions list")
  public List<ProcessDefinitionResponse> getProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.getProcessDefinitions(params);
  }

  @GetMapping("/count")
  @Operation(
      summary = "Retrieve count of all process definitions",
      description = "Returns business process definitions count")
  public CountResponse countProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.countProcessDefinitions(params);
  }

  @PostMapping("/{key}/start")
  @Operation(
      summary = "Start process instance",
      description = "Returns started process instance")
  @ApiResponse(
      description = "Returns started process instance",
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = StartProcessInstanceResponse.class)))
  @ApiResponse(
      description = "Business process definition hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ResponseBody
  public StartProcessInstanceResponse startProcessInstance(@PathVariable("key") String key, Authentication authentication) {
    return processDefinitionService.startProcessInstance(key, authentication);
  }

  @PostMapping("/{key}/start-with-form")
  @Operation(
      summary = "Start process instance with form",
      description = "Returns started process instance")
  @ApiResponse(
      description = "Returns started process instance",
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = StartProcessInstanceResponse.class)))
  @ApiResponse(
      description = "Business process definition hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ApiResponse(
      description = "Internal server error",
      responseCode = "500",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  @ResponseBody
  public StartProcessInstanceResponse startProcessInstanceWithForm(@PathVariable("key") String key,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    return processDefinitionService.startProcessInstanceWithForm(key, formDataDto, authentication);
  }
}
