package com.epam.digital.data.platform.usrprcssmgt.controller;

import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeCitizen;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeOfficer;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.swagger.PageableAsQueryParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProcessInstanceController {

  @Autowired
  private ProcessInstanceApi processInstanceApi;

  @PreAuthorizeAnySystemRole
  @GetMapping("/process-instance/count")
  @Operation(
      summary = "Retrieve count of all unfinished process instances wiht root process instance",
      description = "Returns business process instances count")
  public CountResultDto countProcessInstances() {
    return processInstanceApi.countProcessInstances();
  }

  @PreAuthorizeOfficer
  @GetMapping("/officer/process-instance")
  @Operation(
      summary = "Retrieve all process instances for the officer role",
      description = "Returns business process instances list")
  @ApiResponse(
      description = "Business process instances list",
      responseCode = "200",
      content = @Content(array = @ArraySchema(uniqueItems = true,
          schema = @Schema(implementation = GetProcessInstanceResponse.class)),
          examples = @ExampleObject(
              summary = "Process instance array",
              description = "Set of unfinished process instances",
              value = "[{\"id\":\"4ce5cc26-33ab-11eb-adc1-0242ac120002\",\n" +
                  "\"processDefinitionId\":\"processDefinitionId\",\n" +
                  "\"processDefinitionName\":\"processDefinition\",\n" +
                  "\"startTime\":\"2020-12-01T12:00:00\",\n" +
                  "\"status\":{\"code\":\"in_progress\", \"title\":\"У виконанні\"}}]")))
  @PageableAsQueryParam
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(@Parameter(hidden = true) Pageable page) {
    return processInstanceApi.getOfficerProcessInstances(page);
  }

  @PreAuthorizeCitizen
  @GetMapping("/citizen/process-instance")
  @Operation(
      summary = "Retrieve all process instances for the citizen role",
      description = "Returns business process instances list")
  @ApiResponse(
      description = "Business process instances list",
      responseCode = "200",
      content = @Content(array = @ArraySchema(uniqueItems = true,
          schema = @Schema(implementation = GetProcessInstanceResponse.class)),
          examples = @ExampleObject(
              summary = "Process instance array",
              description = "Set of unfinished process instances",
              value = "[{\"id\":\"4ce5cc26-33ab-11eb-adc1-0242ac120002\",\n" +
                  "\"processDefinitionId\":\"processDefinitionId\",\n" +
                  "\"processDefinitionName\":\"processDefinition\",\n" +
                  "\"startTime\":\"2020-12-01T12:00:00\",\n" +
                  "\"status\":{\"code\":\"citizen_in_progress\", \"title\":\"Прийнято в обробку\"}}]")))
  @PageableAsQueryParam
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(@Parameter(hidden = true) Pageable page) {
    return processInstanceApi.getCitizenProcessInstances(page);
  }
}
