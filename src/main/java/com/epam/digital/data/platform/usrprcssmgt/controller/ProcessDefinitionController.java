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
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.ProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@Tag(description = "User process definition Rest API", name = "user-process-definition-api")
public class ProcessDefinitionController {

  private final ProcessDefinitionService processDefinitionService;

  @GetMapping("/{key}")
  @Operation(
      summary = "Retrieve process definition by key",
      description = "### Endpoint purpose:\n This endpoint allows you to retrieve a process definition based on its unique key.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Process definition",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = ProcessDefinitionResponse.class),
                  examples = @ExampleObject(value = "{\n"
                      + "  \"id\": \"ea4430c8-66c2-11ee-b586-0a580a80065a\",\n"
                      + "  \"key\": \"business-process-key\",\n"
                      + "  \"name\": \"Business process name\",\n"
                      + "  \"suspended\": false,\n"
                      + "  \"formKey\": null\n"
                      + "}"))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Business process definition hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))
          )
      }
  )
  public ProcessDefinitionResponse getProcessDefinitionByKey(@PathVariable("key") String key) {
    return processDefinitionService.getProcessDefinitionByKey(key);
  }

  @GetMapping
  @Operation(
      summary = "Retrieve all process definitions",
      description = "### Endpoint purpose:\n This endpoint allows to retrieve a list of process definitions based on the provided parameters, like _active_ or _suspended_ query parameters",
      parameters = {
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              in = ParameterIn.QUERY,
              name = "suspended",
              description = "Parameter used to retrieve suspended processes",
              schema = @Schema(type = "boolean")
          ),
          @Parameter(
              in = ParameterIn.QUERY,
              name = "active",
              description = "Parameter used to retrieve active processes",
              schema = @Schema(type = "boolean")
          )
      },
      responses = {
          @ApiResponse(
              description = "List of process definitions",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = ProcessDefinitionResponse.class),
                  examples = @ExampleObject(value = "[{\n"
                      + "  \"id\": \"ea4430c8-66c2-11ee-b586-0a580a80065a\",\n"
                      + "  \"key\": \"business-process-key\",\n"
                      + "  \"name\": \"Business process name\",\n"
                      + "  \"suspended\": false,\n"
                      + "  \"formKey\": null\n"
                      + "}]"))
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
  public List<ProcessDefinitionResponse> getProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.getProcessDefinitions(params);
  }

  @GetMapping("/count")
  @Operation(
      summary = "Retrieve count of process definitions",
      description = "### Endpoint purpose:\n This endpoint allows you to retrieve the total count of available process definitions that match the specified parameters. You can filter the count by specifying criteria like _active_ or _suspended_ query parameters",
      parameters = {
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              in = ParameterIn.QUERY,
              name = "suspended",
              description = "Parameter used to retrieve suspended processes",
              schema = @Schema(type = "boolean")
          ),
          @Parameter(
              in = ParameterIn.QUERY,
              name = "active",
              description = "Parameter used to retrieve active processes",
              schema = @Schema(type = "boolean")
          )
      },
      responses = {
          @ApiResponse(
              description = "Count of process definitions",
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
  public CountResponse countProcessDefinitions(GetProcessDefinitionsParams params) {
    return processDefinitionService.countProcessDefinitions(params);
  }

  @PostMapping("/{key}/start")
  @Operation(
      summary = "Start process instance",
      description = "### Endpoint purpose:\n This endpoint allows you to initiate a new process instance based on the provided process definition key",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "Returns started process instance",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = StartProcessInstanceResponse.class),
                  examples = @ExampleObject(value = "{\n"
                      + "  \"id\": \"d81fd894-6842-11ee-b71c-0a580a811836\",\n"
                      + "  \"processDefinitionId\": \"fcfea78f-66c2-11ee-b586-0a580a80065a\",\n"
                      + "  \"ended\": false\n"
                      + "}"))),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              description = "Business process definition hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      })
  @ResponseBody
  public StartProcessInstanceResponse startProcessInstance(@PathVariable("key") String key,
      Authentication authentication) {
    return processDefinitionService.startProcessInstance(key, authentication);
  }

  @PostMapping("/{key}/start-with-form")
  @Operation(
      summary = "Start process instance with form",
      description = "### Endpoint purpose:\n This endpoint allows to start process instance by process definition key with start form data\n"
          + "### Form validation:\n This endpoint requires valid form, if form provided in request body does not match form structure assigned to task, then _422_ status code returned.",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FormDataDto.class),
              examples = {
                  @ExampleObject(value = "{\n"
                      + "  \"data\": {\n"
                      + "     \"formFieldName1\": \"field value 1\",\n"
                      + "     \"formFieldName2\": \"field value 2\"\n"
                      + "}}"
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Returns started process instance",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = StartProcessInstanceResponse.class),
                  examples = @ExampleObject(value = "{\n"
                      + "  \"id\": \"d81fd894-6842-11ee-b71c-0a580a811836\",\n"
                      + "  \"processDefinitionId\": \"fcfea78f-66c2-11ee-b586-0a580a80065a\",\n"
                      + "  \"ended\": false\n"
                      + "}"))),
          @ApiResponse(
              description = "Business process definition hasn't found",
              responseCode = "404",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class))),
          @ApiResponse(
              description = "Form validation failed",
              responseCode = "422",
              content = @Content(schema = @Schema(implementation = ValidationErrorDto.class))),
          @ApiResponse(
              description = "Internal server error",
              responseCode = "500",
              content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
      })
  @ResponseBody
  public StartProcessInstanceResponse startProcessInstanceWithForm(@PathVariable("key") String key,
      @RequestBody FormDataDto formDataDto, Authentication authentication) {
    return processDefinitionService.startProcessInstanceWithForm(key, formDataDto, authentication);
  }
}
