package com.epam.digital.data.platform.usrprcssmgt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.ProcessDefinitionQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import com.epam.digital.data.platform.bpms.client.ProcessDefinitionRestClient;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessDefinitionsParams;
import java.util.Collections;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;
  @Mock
  private ProcessDefinitionRestClient processDefinitionRestClient;

  @Test
  void countProcessDefinitions() {
    var processDefinitionQuery = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .build();
    var expectedCountDto = new CountResultDto(7L);
    when(processDefinitionRestClient.getProcessDefinitionsCount(processDefinitionQuery))
        .thenReturn(expectedCountDto);

    var result = processDefinitionService.countProcessDefinitions(
        new GetProcessDefinitionsParams());

    assertThat(result.getCount()).isEqualTo(7L);
  }

  @Test
  void getProcessDefinitions() {
    var processDefinitionQuery = ProcessDefinitionQueryDto.builder()
        .latestVersion(true)
        .active(true)
        .suspended(false)
        .sortBy(ProcessDefinitionQueryDto.SortByConstants.SORT_BY_NAME)
        .sortOrder(SortOrder.ASC.stringValue()).build();
    var definition = DdmProcessDefinitionDto.builder()
        .id("id")
        .name("Awesome Definition Name")
        .formKey("testFormKey")
        .build();
    when(processDefinitionRestClient.getProcessDefinitionsByParams(processDefinitionQuery))
        .thenReturn(Collections.singletonList(definition));

    var result = processDefinitionService.getProcessDefinitions(new GetProcessDefinitionsParams());

    assertThat(result).hasSize(1)
        .element(0).isSameAs(definition);
  }

  @Test
  void getProcessDefinitionByKey() {
    var processDefinitionId = "id";
    var processDefinitionKey = "processDefinitionKey";

    var definition = DdmProcessDefinitionDto.builder()
        .id(processDefinitionId)
        .key(processDefinitionKey)
        .name("testName")
        .formKey("testFormKey")
        .build();
    when(processDefinitionRestClient.getProcessDefinitionByKey(processDefinitionKey))
        .thenReturn(definition);

    var result = processDefinitionService.getProcessDefinitionByKey(processDefinitionKey);

    assertThat(result).isSameAs(definition);
  }
}
