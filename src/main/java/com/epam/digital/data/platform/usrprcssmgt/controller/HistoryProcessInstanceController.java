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
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.HistoryUserProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.controller.swagger.PageableAsQueryParam;
import com.epam.digital.data.platform.usrprcssmgt.service.HistoryProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorizeAnySystemRole
@RequestMapping("/api/history/process-instance")
@RequiredArgsConstructor
public class HistoryProcessInstanceController {

  private final HistoryProcessInstanceService historyProcessInstanceService;

  @GetMapping
  @Operation(
      summary = "Retrieve all history process instances",
      description = "Returns history business process instances list")
  @ApiResponse(
      description = "History business process instances list",
      responseCode = "200",
      content = {
          @Content(array = @ArraySchema(uniqueItems = true,
              schema = @Schema(implementation = HistoryUserProcessInstanceResponse.class)),
              examples = {@ExampleObject(
                  summary = "History process instances array",
                  description = "Set of history process instances",
                  value = "[{ \"id\": \"4ce5cc26-33ab-11eb-adc1-0242ac120002\"," +
                      " \"processDefinitionId\": \"processDefinitionId\"," +
                      " \"processDefinitionName\": \"processDefinition\", " +
                      "\"startTime\": \"2020-12-01T12:00:00\"," +
                      " \"endTime\": \"2020-12-01T12:00:00\"," +
                      "\"status\":{\"code\":\"COMPLETED\" } }]"
              )}
          )}
  )
  @PageableAsQueryParam
  public List<HistoryUserProcessInstanceResponse> getHistoryProcessInstances(
      @Parameter(hidden = true) Pageable page) {
    return historyProcessInstanceService.getHistoryProcessInstances(page);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get history process instance by id",
      description = "Returns history process instance by id")
  @ApiResponse(
      description = "Returns history process instance",
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = HistoryUserProcessInstanceResponse.class),
          examples = {@ExampleObject(
              summary = "History process instance",
              description = "History process instance",
              value = "{ \"id\": \"746c7c8e-3302-11eb-aafe-165da9830012\"," +
                  " \"processDefinitionId\": \"Process_00rzvvo:1:01c60bc9-32f3-11eb-aafe-165da9830012\","
                  +
                  " \"processDefinitionName\": \"Заява про реєстрацію медичної ліцензії\", " +
                  "\"startTime\": \"2020-11-30T11:52:00\"," +
                  " \"endTime\": \"2020-12-01T12:00:00\"," +
                  "\"status\":{\"code\":\"COMPLETED\" } }"
          )}
      )
  )
  @ApiResponse(
      description = "History process instance hasn't found",
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = SystemErrorDto.class)))
  public HistoryUserProcessInstanceResponse getHistoryProcessInstanceById(
      @PathVariable("id") String id) {
    return historyProcessInstanceService.getHistoryProcessInstanceById(id);
  }
}
