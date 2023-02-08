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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorizeAnySystemRole
@RequestMapping("/api/grouped-process-definition")
public class GroupedProcessDefinitionController {

  private final ProcessDefinitionService processDefinitionService;


  @GetMapping
  @Operation(
      summary = "Retrieve all process definitions with groups",
      description = "Returns grouped and ungrouped business process definitions ordered lists")
  public GroupedProcessDefinitionResponse getProcessDefinitions(
      GetProcessDefinitionsParams params) {
    return processDefinitionService.getGroupedProcessDefinitions(params);
  }
}
