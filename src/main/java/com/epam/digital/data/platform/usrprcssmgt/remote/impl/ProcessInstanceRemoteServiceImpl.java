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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.starter.security.SystemRole;
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

  private final BaseMapper baseMapper;
  private final ProcessInstanceMapper processInstanceMapper;

  @Override
  public CountResponse countProcessInstances() {
    log.debug("Selecting count of unfinished process instances from bpms");

    var queryDto = DdmProcessInstanceCountQueryDto.builder()
        .rootProcessInstances(true)
        .build();
    var result = processInstanceRestClient.getProcessInstancesCount(queryDto);

    return baseMapper.toCountResponse(result);
  }

  @Override
  public List<GetProcessInstanceResponse> getProcessInstances(Pageable page,
      SystemRole systemRole) {
    log.debug("Selecting unfinished {} process instances. Parameters: {}", systemRole, page);

    var processInstances = getCamundaProcessInstances(page);

    log.debug("Found {} unfinished {} process instances. {}", processInstances.size(), systemRole,
        processInstances);
    return processInstanceMapper.toProcessInstanceResponses(processInstances, systemRole);
  }

  private List<DdmProcessInstanceDto> getCamundaProcessInstances(Pageable page) {
    var queryDto = DdmProcessInstanceQueryDto.builder()
        .rootProcessInstances(true)
        .sortBy(page.getSortBy())
        .sortOrder(page.getSortOrder())
        .build();
    var paginationQueryDto = PaginationQueryDto.builder()
        .firstResult(page.getFirstResult())
        .maxResults(page.getMaxResults())
        .build();
    return processInstanceRestClient.getProcessInstances(queryDto, paginationQueryDto);
  }
}
