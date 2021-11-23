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

package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessDefinitionApi;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessDefinitionApi}. A proxy to the {@link
 * ProcessDefinitionRestClient} that also maps the camunda response to the needed format.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService implements ProcessDefinitionApi {

  private final ProcessDefinitionRestClient processDefinitionRestClient;

  @Override
  public DdmProcessDefinitionDto getProcessDefinitionByKey(String key) {
    log.info("Getting process definition by key - {}", key);

    var result = processDefinitionRestClient.getProcessDefinitionByKey(key);
    log.trace("Found process definition - {}", result);

    log.info("Process definition with key {} is found", key);
    return result;
  }

  @Override
  public List<DdmProcessDefinitionDto> getProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting list of process definitions. Params: {}", params);

    var queryDto = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue())
        .build();
    var result = processDefinitionRestClient.getProcessDefinitionsByParams(queryDto);
    log.trace("Found process definitions - {}", result);

    log.info("List of process definitions is found. Size - {}", result.size());
    return result;
  }

  @Override
  public CountResultDto countProcessDefinitions(GetProcessDefinitionsParams params) {
    log.info("Getting count of process definitions. Params: {}", params);

    var queryDto = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(params.isActive())
        .suspended(params.isSuspended())
        .build();
    var result = processDefinitionRestClient.getProcessDefinitionsCount(queryDto);

    log.info("Count of process definitions is found - {}", result.getCount());
    return result;
  }
}
