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

package com.epam.digital.data.platform.usrprcssmgt.remote.impl;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryProcessInstanceRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.usrprcssmgt.mapper.BaseMapper;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.request.Pageable;
import com.epam.digital.data.platform.usrprcssmgt.model.response.CountResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.remote.ProcessInstanceRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceRemoteServiceImpl implements ProcessInstanceRemoteService {

  private final ProcessInstanceRestClient processInstanceRestClient;
  private final HistoryProcessInstanceRestClient historyProcessInstanceRestClient;

  private final BaseMapper baseMapper;
  private final ProcessInstanceMapper processInstanceMapper;

  @Override
  public CountResponse countProcessInstances() {
    log.debug("Selecting count of unfinished process instances from bpms");

    var queryDto = ProcessInstanceCountQueryDto.builder()
        .rootProcessInstances(true)
        .build();
    var result = processInstanceRestClient.getProcessInstancesCount(queryDto);

    return baseMapper.toCountResponse(result);
  }

  @Override
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page) {
    log.debug("Selecting unfinished officer process instances. Parameters: {}", page);

    var processInstances = getCamundaProcessInstances(page);

    log.debug("Found {} unfinished officer process instances. {}", processInstances.size(),
        processInstances);
    return processInstanceMapper.toOfficerProcessInstanceResponses(processInstances);
  }

  @Override
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page) {
    log.debug("Selecting unfinished citizen process instances. Parameters: {}", page);

    var processInstances = getCamundaProcessInstances(page);

    log.debug("Found {} unfinished citizen process instances. {}", processInstances.size(),
        processInstances);
    return processInstanceMapper.toCitizenProcessInstanceResponses(processInstances);
  }

  private List<HistoryProcessInstanceDto> getCamundaProcessInstances(Pageable page) {
    var processInstanceQueryDto = HistoryProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .unfinished(true)
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();
    return historyProcessInstanceRestClient.getHistoryProcessInstanceDtosByParams(
        processInstanceQueryDto, paginationQueryDto);
  }
}
