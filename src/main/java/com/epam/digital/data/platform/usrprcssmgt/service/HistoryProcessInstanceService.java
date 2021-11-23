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

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryProcessInstanceRestClient;
import com.epam.digital.data.platform.usrprcssmgt.api.HistoryProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.mapper.HistoryProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryUserProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link HistoryProcessInstanceApi}. A proxy to the {@link
 * HistoryProcessInstanceRestClient} that also maps the camunda response to the needed format and
 * localizes it if needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryProcessInstanceService implements HistoryProcessInstanceApi {

  private final HistoryProcessInstanceRestClient historyProcessInstanceRestClient;
  private final HistoryProcessInstanceMapper historyProcessInstanceMapper;

  @Override
  public List<HistoryUserProcessInstance> getHistoryProcessInstances(Pageable page) {
    log.info("Getting finished process instances. Parameters: {}", page);

    var historyProcessInstances = getCamundaProcessInstances(page);
    log.trace("Found {} Camunda process instances", historyProcessInstances.size());

    var result = historyProcessInstanceMapper.toHistoryProcessInstances(historyProcessInstances);
    log.trace("Process instances filled with addition info - {}", result);

    log.info("{} process instances are found", result.size());
    return result;
  }

  @Override
  public HistoryUserProcessInstance getHistoryProcessInstanceById(String processInstanceId) {
    log.info("Get finished process instance by id {}", processInstanceId);

    var processInstance = historyProcessInstanceRestClient.getProcessInstanceById(
        processInstanceId);
    log.trace("Found Camunda process instance");

    var result = historyProcessInstanceMapper.toHistoryProcessInstance(processInstance);
    log.trace("Process instance filled with addition info - {}", processInstance);

    return result;
  }

  @Override
  public CountResultDto getCountProcessInstances() {
    log.info("Getting count of finished process instances");

    var queryDto = HistoryProcessInstanceCountQueryDto.builder()
        .finished(true)
        .rootProcessInstances(true)
        .build();
    var result = historyProcessInstanceRestClient.getProcessInstancesCount(queryDto);

    log.info("Count of finished process instances is found - {}", result.getCount());
    return result;
  }

  private List<HistoryProcessInstanceDto> getCamundaProcessInstances(Pageable page) {
    var historyProcessInstanceQueryDto = HistoryProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .finished(true)
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();
    return historyProcessInstanceRestClient.getHistoryProcessInstanceDtosByParams(
        historyProcessInstanceQueryDto, paginationQueryDto);
  }
}
