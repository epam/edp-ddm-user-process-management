package com.epam.digital.data.platform.usrprcssmgt.controller;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorizeAnySystemRole
@RequestMapping("/api/process-definition")
public class ProcessDefinitionController {

  @Autowired
  private ProcessDefinitionService processDefinitionService;

  @GetMapping("/{id}")
  @Operation(
      summary = "Retrieve process definition by id",
      description = "Returns business process definition entity")
  public UserProcessDefinitionDto getProcessDefinitionById(@PathVariable("id") String id) {
    return processDefinitionService.getProcessDefinitionById(id);
  }

  @GetMapping
  @Operation(
      summary = "Retrieve all process definitions",
      description = "Returns business process definitions list")
  public List<UserProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.getProcessDefinitions(params);
  }

  @GetMapping("/count")
  @Operation(
      summary = "Retrieve count of all process definitions",
      description = "Returns business process definitions count")
  public CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.countProcessDefinitions(params);
  }

  @PostMapping("/{id}/start")
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
  public StartProcessInstanceResponse startProcessInstance(@PathVariable("id") String id) {
    return processDefinitionService.startProcessDefinition(id);
  }

  @PostMapping("/{id}/start-with-form")
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
  public StartProcessInstanceResponse startProcessInstanceWithForm(@PathVariable("id") String id,
      @RequestBody FormDataDto formDataDto,
      @RequestHeader("x-access-token") String accessToken) {
    formDataDto.setAccessToken(accessToken);
    return processDefinitionService.startProcessDefinitionWithForm(id, formDataDto);
  }
}
