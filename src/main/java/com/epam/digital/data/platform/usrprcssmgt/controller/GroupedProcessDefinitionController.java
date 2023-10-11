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

import com.epam.digital.data.platform.starter.security.annotation.PreAuthorizeAnySystemRole;
import com.epam.digital.data.platform.usrprcssmgt.model.request.GetProcessDefinitionsParams;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GroupedProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.service.ProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorizeAnySystemRole
@RequestMapping("/api/grouped-process-definition")
@Tag(description = "Grouped user process definition Rest API", name = "grouped-user-process-definition-api")
public class GroupedProcessDefinitionController {

  private final ProcessDefinitionService processDefinitionService;


  @GetMapping
  @Operation(
      summary = "Retrieve all process definitions with groups",
      description = "### Endpoint purpose:\n This endpoint allows users to retrieve grouped and ungrouped business process definitions ordered lists based on their system role in X-Access-Token",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              description = "List of process definitions with groups",
              responseCode = "200",
              content = @Content(schema = @Schema(implementation = GroupedProcessDefinitionResponse.class),
                  examples = {
                      @ExampleObject(value = "{\n"
                          + "    \"groups\": [\n"
                          + "        {\n"
                          + "            \"name\": \"Business processes group name\",\n"
                          + "            \"processDefinitions\": [\n"
                          + "                {\n"
                          + "                    \"id\": \"fcfea78f-66c2-11ee-b586-0a580a80065a\",\n"
                          + "                    \"key\": \"business-process-in-group\",\n"
                          + "                    \"name\": \"Business process in group name\",\n"
                          + "                    \"suspended\": false,\n"
                          + "                    \"formKey\": null\n"
                          + "                }\n"
                          + "            ]\n"
                          + "        },\n"
                          + "    \"ungrouped\": [\n"
                          + "        {\n"
                          + "            \"id\": \"fcd4151b-66c2-11ee-b586-0a580a80065a\",\n"
                          + "            \"key\": \"ungrouped-process\",\n"
                          + "            \"name\": \"Ungrouped process name\",\n"
                          + "            \"suspended\": false,\n"
                          + "            \"formKey\": null\n"
                          + "        }\n"
                          + "    ]\n"
                          + "}")
                  })),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          )
      }
  )
  public GroupedProcessDefinitionResponse getProcessDefinitions(
      GetProcessDefinitionsParams params) {
    return processDefinitionService.getGroupedProcessDefinitions(params);
  }
}
