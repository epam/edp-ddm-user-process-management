/*
 * Copyright 2023 EPAM Systems.
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
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeCitizen;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeOfficer;
import com.epam.digital.data.platform.usrprcssmgt.controller.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(description = "User process instance Rest API", name = "user-process-instance-api")
public class ProcessInstanceController {

  @Autowired
  private ProcessInstanceService processInstanceService;

  @PreAuthorizeAnySystemRole
  @GetMapping("/process-instance/count")
  @Operation(
      summary = "Returns business process instances count",
      description = "### Endpoint purpose:\n This endpoint allows to retrieve count of all unfinished process instances with root process instance",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Count of process instances",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = CountResponse.class),
                  examples = @ExampleObject(value = "{\n"
                      + "  \"count\": 10\n"
                      + "}"))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))
          )
      })
  public CountResponse countProcessInstances() {
    return processInstanceService.countProcessInstances();
  }

  @PreAuthorizeOfficer
  @GetMapping("/officer/process-instance")
  @Operation(
      summary = "Retrieve all process instances for the officer role",
      description = "### Endpoint purpose:\n Retrieve a list of process instances assigned to the currently authenticated officer user. This endpoint returns a paginated list of process instances that are assigned to the authenticated officer user. The provided pageable parameters allow for customization of pagination settings.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
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
                          "\"status\":{\"code\":\"in_progress\", \"title\":\"У виконанні\"}}]"))),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))
          )
      })

  @PageableAsQueryParam
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(
      @Parameter(hidden = true) Pageable page) {
    return processInstanceService.getOfficerProcessInstances(page);
  }

  @PreAuthorizeCitizen
  @GetMapping("/citizen/process-instance")
  @Operation(
      summary = "Retrieve all process instances for the citizen role",
      description = "### Endpoint purpose:\n Retrieve a list of process instances assigned to the currently authenticated citizen user. This endpoint returns a paginated list of process instances that are assigned to the authenticated citizen user. The provided pageable parameters allow for customization of pagination settings.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
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
                          "\"status\":{\"code\":\"citizen_in_progress\", \"title\":\"Прийнято в обробку\"}}]"))),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))
          )
      })
  @PageableAsQueryParam
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(
      @Parameter(hidden = true) Pageable page) {
    return processInstanceService.getCitizenProcessInstances(page);
  }
}
