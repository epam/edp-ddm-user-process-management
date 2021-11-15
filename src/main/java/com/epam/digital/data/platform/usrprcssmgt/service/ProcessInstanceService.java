package com.epam.digital.data.platform.usrprcssmgt.service;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.ProcessInstanceCountQueryDto;
import com.epam.digital.data.platform.bpms.client.HistoryProcessInstanceRestClient;
import com.epam.digital.data.platform.bpms.client.ProcessInstanceRestClient;
import com.epam.digital.data.platform.usrprcssmgt.api.ProcessInstanceApi;
import com.epam.digital.data.platform.usrprcssmgt.mapper.ProcessInstanceMapper;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.Pageable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.springframework.stereotype.Service;

/**
 * Base implementation of {@link ProcessInstanceApi}. A proxy to the {@link
 * ProcessInstanceRestClient} that also maps the camunda response to the needed format and localizes
 * it if needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService implements ProcessInstanceApi {

  private final ProcessInstanceRestClient processInstanceRestClient;
  private final HistoryProcessInstanceRestClient historyProcessInstanceRestClient;

  private final ProcessInstanceMapper processInstanceMapper;

  @Override
  public CountResultDto countProcessInstances() {
    log.info("Getting count of unfinished process instances");

    var queryDto = ProcessInstanceCountQueryDto.builder()
        .rootProcessInstances(true)
        .build();
    var result = processInstanceRestClient.getProcessInstancesCount(queryDto);

    log.info("Count of unfinished process instances is found - {}", result.getCount());
    return result;
  }

  @Override
  public List<GetProcessInstanceResponse> getOfficerProcessInstances(Pageable page) {
    log.info("Getting unfinished officer process instances. Parameters: {}", page);

    var processInstances = getCamundaProcessInstances(page);
    log.trace("Found {} running camunda process instances", processInstances.size());

    var result = processInstanceMapper.toOfficerProcessInstanceResponses(processInstances);

    log.info("Found {} unfinished officer process instances", result.size());
    return result;
  }

  @Override
  public List<GetProcessInstanceResponse> getCitizenProcessInstances(Pageable page) {
    log.info("Getting unfinished citizen process instances. Parameters: {}", page);

    var processInstances = getCamundaProcessInstances(page);
    log.trace("Found {} running camunda process instances", processInstances.size());

    var result = processInstanceMapper.toCitizenProcessInstanceResponses(processInstances);

    log.info("Found {} unfinished citizen process instances", result.size());
    return result;
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
