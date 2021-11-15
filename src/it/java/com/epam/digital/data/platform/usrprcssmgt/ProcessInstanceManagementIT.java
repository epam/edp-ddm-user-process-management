package com.epam.digital.data.platform.usrprcssmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryUserProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.StubRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class ProcessInstanceManagementIT extends BaseIT {

  @Test
  void countProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/process-instance/count"))
        .queryParams(Map.of("rootProcessInstances", equalTo("true")))
        .status(200)
        .responseBody("{ \"count\": 6 }")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/process-instance/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, CountResultDto.class);

    assertThat(result.getCount()).isEqualTo(6L);
  }

  @Test
  void countHistoryProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/history/process-instance/count"))
        .queryParams(Map.of("finished", equalTo("true"),
            "rootProcessInstances", equalTo("true")))
        .status(200)
        .responseBody("{ \"count\": 42 }")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/history/process-instance/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, CountResultDto.class);

    assertThat(result.getCount()).isEqualTo(42L);
  }

  @Test
  void getOfficerProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"desc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"name\",\"rootProcessInstances\":true}"))
        .queryParams(Map.of("firstResult", equalTo("10"),
            "maxResults", equalTo("42")))
        .status(200)
        .responseBody("[ { \"id\": \"id1\", \"processDefinitionName\":\"processDefinition1\", " +
            "\"startTime\":\"2020-12-01T12:00:00.000Z\" } ]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/officer/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "42")
        .queryParam("sortOrder", "desc")
        .queryParam("sortBy", "name")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinition1")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0));
  }

  @Test
  void shouldReturnOfficerProcessInstancesInPendingStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"PENDING\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.PENDING))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.PENDING);
  }

  @Test
  void shouldReturnOfficerProcessInstanceInSuspendedStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"SUSPENDED\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.SUSPENDED))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.SUSPENDED);
  }

  @Test
  void shouldReturnOfficerProcessInstanceInProgressStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"ACTIVE\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/officer/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.IN_PROGRESS))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.IN_PROGRESS);
  }

  @Test
  void getCitizenProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .queryParams(Map.of("firstResult", equalTo("10"),
            "maxResults", equalTo("42")))
        .status(200)
        .responseBody("[ { \"id\": \"id1\", \"processDefinitionName\":\"processDefinition1\", " +
            "\"startTime\":\"2020-12-01T12:00:00.000Z\" } ]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/citizen/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "42")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsCitizen(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinition1")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0));
  }

  @Test
  void shouldReturnCitizenProcessInstancesInPendingStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"PENDING\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsCitizen(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_PENDING))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.CITIZEN_PENDING);
  }

  @Test
  void shouldReturnCitizenProcessInstanceInSuspendedStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"SUSPENDED\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsCitizen(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_SUSPENDED))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.CITIZEN_SUSPENDED);
  }

  @Test
  void shouldReturnCitizenProcessInstanceInProgressStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"unfinished\":true,"
            + "\"finished\":false,\"sortBy\":\"startTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[{\"id\":\"processInstanceId\",\"state\":\"ACTIVE\"}]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/citizen/process-instance")
        .queryParam("sortOrder", "asc")
        .queryParam("sortBy", "startTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsCitizen(request, GetProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0].getStatus()).isNotNull()
        .hasFieldOrPropertyWithValue("title",
            messageResolver.getMessage(ProcessInstanceStatus.CITIZEN_IN_PROGRESS))
        .hasFieldOrPropertyWithValue("code", ProcessInstanceStatus.CITIZEN_IN_PROGRESS);
  }

  @Test
  void getHistoryProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .queryParams(Map.of("firstResult", equalTo("10"),
            "maxResults", equalTo("1")))
        .requestBody(equalToJson("{\"sortOrder\":\"desc\",\"unfinished\":false,"
            + "\"finished\":true,\"sortBy\":\"endTime\",\"rootProcessInstances\":true}"))
        .status(200)
        .responseBody("[ { \"id\": \"id1\", \"name\": \"sys-var-process-completion-result\", " +
            "\"processDefinitionName\":\"processDefinition1\", " +
            "\"startTime\":\"2020-12-01T12:00:00.000Z\", " +
            "\"endTime\":\"2020-12-02T12:00:00.000Z\" } ]")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/history/process-instance")
        .queryParam("firstResult", "10")
        .queryParam("maxResults", "1")
        .queryParam("sortOrder", "desc")
        .queryParam("sortBy", "endTime")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstance[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinition1")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0))
        .hasFieldOrPropertyWithValue("endTime", LocalDateTime.of(2020, 12, 2, 12, 0, 0, 0))
        .extracting(HistoryUserProcessInstance::getStatus)
        .hasFieldOrPropertyWithValue("code", null)
        .hasFieldOrPropertyWithValue("title", null);
  }

  @Test
  void getHistoryProcessInstancesById() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/extended/history/process-instance/testId"))
        .status(200)
        .responseBody("{ \"id\":\"testId\"," +
            "\"processDefinitionId\":\"Process_00rzvvo:1:01c60bc9-32f3-11eb-aafe-165da9830012\"," +
            "\"processDefinitionName\":\"name\"," +
            "\"startTime\":\"2020-11-30T11:52:00.000Z\"," +
            "\"endTime\":\"2020-12-01T12:00:00.000Z\"," +
            "\"state\":\"COMPLETED\"," +
            "\"processCompletionResult\":\"value1\"}")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/history/process-instance/testId")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstance.class);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", "testId")
        .extracting(HistoryUserProcessInstance::getStatus)
        .hasFieldOrPropertyWithValue("code", "COMPLETED")
        .hasFieldOrPropertyWithValue("title", "value1");
  }

  @Test
  void failedGetProcessInstance() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/extended/history/process-instance/testId"))
        .status(404)
        .responseBody("{\"message\":\"Not found\",\"code\":\"404\"}")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = MockMvcRequestBuilders.get("/api/history/process-instance/testId")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficerAndExpect(request, SystemErrorDto.class,
        status().isNotFound());

    assertThat(result.getMessage()).isEqualTo("Not found");
  }

  @Test
  void failedGetProcessInstances_badRequest() {
    mockGetProcessInstancesRequest(400, "{\"message\":\"Bad request\",\"code\":\"400\"}");

    var request = MockMvcRequestBuilders.get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficerAndExpect(request, SystemErrorDto.class,
        status().isBadRequest());

    assertThat(result.getMessage()).isEqualTo("Bad request");
  }

  @Test
  void failedGProcessInstances_unauthorized() {
    mockGetProcessInstancesRequest(401, "{\"message\":\"Unauthorized\",\"code\":\"401\"}");

    var request = MockMvcRequestBuilders.get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficerAndExpect(request, SystemErrorDto.class,
        status().isUnauthorized());

    assertThat(result).isNotNull();
  }

  @Test
  void failedGProcessInstances_forbidden() {
    mockGetProcessInstancesRequest(403, "{\"code\":\"403\",\"message\":\"Forbidden\"}");

    var request = MockMvcRequestBuilders.get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficerAndExpect(request, SystemErrorDto.class,
        status().isForbidden());

    assertThat(result.getMessage()).isEqualTo("Forbidden");
  }

  @Test
  void shouldReturnExcerptId() {
    mockGetProcessInstancesRequest(200, "[{\"id\":\"testId\",\"excerptId\":\"1234\"}]");

    var request = MockMvcRequestBuilders.get("/api/history/process-instance")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstance[].class);

    assertThat(result).isNotNull();
    assertThat(result[0].getExcerptId()).isEqualTo("1234");
  }

  private void mockGetProcessInstancesRequest(int statusCode, String body) {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/history/process-instance"))
        .status(statusCode)
        .responseBody(body)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }
}
