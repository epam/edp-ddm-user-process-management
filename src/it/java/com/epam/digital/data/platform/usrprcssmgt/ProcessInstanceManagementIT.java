package com.epam.digital.data.platform.usrprcssmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.bpms.api.dto.HistoryVariableInstanceQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.TaskQueryDto;
import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryProcessInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class ProcessInstanceManagementIT extends BaseIT {

  @Test
  public void countProcessInstances() {
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/process-instance/count?rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"count\": 6 }"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/process-instance/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole, CountResultDto.class);

    assertThat(result.getCount(), is(6L));
  }

  @Test
  public void countHistoryProcessInstances() {
    bpmServer.addStubMapping(stubFor(get(urlEqualTo("/api/history/process-instance/count"
        + "?finished=true&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody("{ \"count\": 42 }"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole, CountResultDto.class);

    assertThat(result.getCount(), is(42L));
  }

  @Test
  public void getOfficerProcessInstances() throws JsonProcessingException {
    var requestDto = TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("id1"))
        .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/history/process-instance"))
        .withQueryParam("sortOrder", equalTo("desc"))
        .withQueryParam("rootProcessInstances", equalTo("true"))
        .withQueryParam("unfinished", equalTo("true"))
        .withQueryParam("finished", equalTo("false"))
        .withQueryParam("sortBy", equalTo("name"))
        .withQueryParam("firstResult", equalTo("10"))
        .withQueryParam("maxResults", equalTo("42"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "[ { \"id\": \"id1\", \"processDefinitionName\":\"processDefinition1\", " +
                    "\"startTime\":\"2020-12-01T12:00:00.000Z\" } ]"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/officer/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "42")
        .queryParam("sortOrder", "desc")
        .queryParam("sortBy", "name")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getId(), is("id1"));
    assertThat(result[0].getProcessDefinitionName(), is("processDefinition1"));
    assertThat(result[0].getStartTime(), is(LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0)));
  }

  @Test
  public void shouldReturnOfficerProcessInstancesInPendingStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList(processInstanceId))
            .build();
    var task = new TaskEntity();
    task.setProcessInstanceId(processInstanceId);
    var taskDto = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto))))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.PENDING)));
    assertThat(result[0].getStatus().getCode(), is(ProcessInstanceStatus.PENDING.name()));
  }

  @Test
  public void shouldReturnOfficerProcessInstanceInSuspendedStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    processInstanceDto.setState(HistoricProcessInstance.STATE_SUSPENDED);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("processInstanceId"))
            .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.SUSPENDED)));
    assertThat(result[0].getStatus().getCode(), is(ProcessInstanceStatus.SUSPENDED.name()));
  }

  @Test
  public void shouldReturnOfficerProcessInstanceInProgressStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("processInstanceId"))
            .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.IN_PROGRESS)));
    assertThat(result[0].getStatus().getCode(), is(ProcessInstanceStatus.IN_PROGRESS.name()));
  }

  @Test
  public void getCitizenProcessInstances() throws JsonProcessingException {
    var requestDto = TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("id1"))
        .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/history/process-instance"))
        .withQueryParam("sortOrder", equalTo("asc"))
        .withQueryParam("unfinished", equalTo("true"))
        .withQueryParam("finished", equalTo("false"))
        .withQueryParam("sortBy", equalTo("startTime"))
        .withQueryParam("firstResult", equalTo("10"))
        .withQueryParam("maxResults", equalTo("42"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "[ { \"id\": \"id1\", \"processDefinitionName\":\"processDefinition1\", " +
                    "\"startTime\":\"2020-12-01T12:00:00.000Z\" } ]"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/citizen/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "42")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenCitizenRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getId(), is("id1"));
    assertThat(result[0].getProcessDefinitionName(), is("processDefinition1"));
    assertThat(result[0].getStartTime(), is(LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0)));
  }

  @Test
  public void shouldReturnCitizenProcessInstancesInPendingStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList(processInstanceId))
            .build();
    var task = new TaskEntity();
    task.setProcessInstanceId(processInstanceId);
    var taskDto = TaskDto.fromEntity(task);
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto))))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenCitizenRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_PENDING)));
    assertThat(result[0].getStatus().getCode(), is(ProcessInstanceStatus.CITIZEN_PENDING.name()));
  }

  @Test
  public void shouldReturnCitizenProcessInstanceInSuspendedStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    processInstanceDto.setState(HistoricProcessInstance.STATE_SUSPENDED);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("processInstanceId"))
            .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenCitizenRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_SUSPENDED)));
    assertThat(result[0].getStatus().getCode(), is(ProcessInstanceStatus.CITIZEN_SUSPENDED.name()));
  }

  @Test
  public void shouldReturnCitizenProcessInstanceInProgressStatus() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId(processInstanceId);
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    bpmServer.addStubMapping(stubFor(get(urlEqualTo(
        "/api/history/process-instance?sortOrder=asc&unfinished=true&finished=false&sortBy=startTime&rootProcessInstances=true"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto))))));

    var requestDto =
        TaskQueryDto.builder().processInstanceIdIn(Collections.singletonList("processInstanceId"))
            .build();
    bpmServer.addStubMapping(stubFor(post(urlEqualTo("/api/task"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(Lists.newArrayList())))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenCitizenRole,
        GetProcessInstanceResponse[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getStatus(), notNullValue());
    assertThat(result[0].getStatus().getTitle(), notNullValue());
    assertThat(result[0].getStatus().getCode(), notNullValue());
    assertThat(result[0].getStatus().getTitle(),
        is(messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_IN_PROGRESS)));
    assertThat(result[0].getStatus().getCode(),
        is(ProcessInstanceStatus.CITIZEN_IN_PROGRESS.name()));
  }

  @Test
  public void getHistoryProcessInstances() throws JsonProcessingException {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/history/process-instance"))
        .withQueryParam("sortOrder", equalTo("desc"))
        .withQueryParam("finished", equalTo("true"))
        .withQueryParam("sortBy", equalTo("endTime"))
        .withQueryParam("firstResult", equalTo("10"))
        .withQueryParam("maxResults", equalTo("1"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
                "[ { \"id\": \"id1\", \"name\": \"sys-var-process-completion-result\", " +
                    "\"processDefinitionName\":\"processDefinition1\", " +
                    "\"startTime\":\"2020-12-01T12:00:00.000Z\", " +
                    "\"endTime\":\"2020-12-02T12:00:00.000Z\" } ]"))));

    var requestDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(Collections.singletonList("id1")).build();
    bpmServer.addStubMapping(stubFor(post(urlPathEqualTo("/api/history/variable-instance"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("[]"))
    ));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "1")
        .queryParam("sortOrder", "desc")
        .queryParam("sortBy", "endTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        HistoryProcessInstance[].class);

    assertThat(result.length, is(1));
    assertThat(result[0].getId(), is("id1"));
    assertThat(result[0].getProcessDefinitionName(), is("processDefinition1"));
    assertThat(result[0].getStartTime(), is(LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0)));
    assertThat(result[0].getEndTime(), is(LocalDateTime.of(2020, 12, 2, 12, 0, 0, 0)));
    assertThat(result[0].getStatus().getCode(), is(nullValue()));
    assertThat(result[0].getStatus().getTitle(), is(nullValue()));
  }

  @Test
  public void getHistoryProcessInstancesById() throws JsonProcessingException {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/history/process-instance/testId"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
                "{ \"id\": \"testId\"," +
                    " \"processDefinitionId\": \"Process_00rzvvo:1:01c60bc9-32f3-11eb-aafe-165da9830012\","
                    +
                    " \"processDefinitionName\": \"name\", " +
                    "\"startTime\": \"2020-11-30T11:52:00.000Z\"," +
                    " \"endTime\": \"2020-12-01T12:00:00.000Z\"," +
                    "\"state\":\"COMPLETED\" }"))));

    var requestDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceId("testId").build();
    bpmServer.addStubMapping(stubFor(post(urlPathEqualTo("/api/history/variable-instance"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
                "[{\"id\":\"id1\", \"name\": \"sys-var-process-completion-result\", \"processInstanceId\":\"testId\", \"value\":\"value1\"}]"))
    ));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance/testId")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        HistoryProcessInstance.class);

    assertThat(result.getId(), is("testId"));
    assertThat(result.getStatus().getCode(), is("COMPLETED"));
    assertThat(result.getStatus().getTitle(), is("value1"));
  }

  @Test
  public void failedGetProcessInstance() throws Exception {
    var errorDto = new SystemErrorDto();
    errorDto.setMessage("Not found");
    errorDto.setCode("404");

    bpmServer.addStubMapping(stubFor(get("/api/history/process-instance/testId")
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(errorDto)))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance/testId")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performWithTokenOfficerRole(request).andExpect(status().isNotFound()).andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    Assertions.assertThat(resultBody.getMessage()).isEqualTo("Not found");
  }

  @Test
  public void failedGetProcessInstances_badRequest() throws Exception {
    var errorDto = new SystemErrorDto();
    errorDto.setMessage("Bad request");
    errorDto.setCode("400");

    mockGetProcessInstancesRequest(400, objectMapper.writeValueAsString(errorDto));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performWithTokenOfficerRole(request).andExpect(status().isBadRequest())
        .andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    Assertions.assertThat(resultBody.getMessage()).isEqualTo("Bad request");
  }

  @Test
  public void failedGProcessInstances_unauthorized() throws Exception {
    var errorDto = new SystemErrorDto();
    errorDto.setMessage("Unauthorized");
    errorDto.setCode("401");

    mockGetProcessInstancesRequest(401, objectMapper.writeValueAsString(errorDto));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performWithTokenOfficerRole(request).andExpect(status().isUnauthorized())
        .andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    Assertions.assertThat(resultBody.getMessage()).isEqualTo("Unauthorized");
  }

  @Test
  public void failedGProcessInstances_forbidden() throws Exception {
    mockGetProcessInstancesRequest(403, "{ \"code\":\"403\",\"message\":\"Forbidden\" }");

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performWithTokenOfficerRole(request).andExpect(status().isForbidden()).andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    Assertions.assertThat(resultBody.getMessage()).isEqualTo("Forbidden");
  }

  @Test
  public void shouldReturnExcerptId() throws JsonProcessingException {
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("testId");
    var processInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);

    mockGetProcessInstancesRequest(200,
        objectMapper.writeValueAsString(Lists.newArrayList(processInstanceDto)));

    var requestDto = HistoryVariableInstanceQueryDto.builder()
        .variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)
        .processInstanceIdIn(Collections.singletonList("testId")).build();
    bpmServer.addStubMapping(stubFor(post(urlPathEqualTo("/api/history/variable-instance"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("[{\"id\":\"id1\", \"name\": \"sys-var-process-excerpt-id\", "
                    + "\"processInstanceId\":\"testId\", \"value\":\"1234\"}]"))
    ));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        HistoryProcessInstance[].class);

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result[0].getExcerptId()).isEqualTo("1234");
  }

  private void mockGetProcessInstancesRequest(int statusCode, String body) {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/history/process-instance"))
        .willReturn(aResponse()
            .withStatus(statusCode)
            .withHeader("Content-Type", "application/json")
            .withBody(body))));
  }
}
