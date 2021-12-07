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

package com.epam.digital.data.platform.usrprcssmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.i18n.ProcessInstanceStatusMessageTitle;
import com.epam.digital.data.platform.usrprcssmgt.model.StubRequest;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.HistoryUserProcessInstanceResponse;
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
  void getOfficerProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson(
            "{\"sortOrder\":\"desc\",\"sortBy\":\"name\",\"rootProcessInstances\":true}"))
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
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.PENDING))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.PENDING);
  }

  @Test
  void shouldReturnOfficerProcessInstanceInSuspendedStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.SUSPENDED))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.SUSPENDED);
  }

  @Test
  void shouldReturnOfficerProcessInstanceInProgressStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.IN_PROGRESS))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.ACTIVE);
  }

  @Test
  void getCitizenProcessInstances() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.CITIZEN_PENDING))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.PENDING);
  }

  @Test
  void shouldReturnCitizenProcessInstanceInSuspendedStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.CITIZEN_SUSPENDED))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.SUSPENDED);
  }

  @Test
  void shouldReturnCitizenProcessInstanceInProgressStatus() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-instance"))
        .requestBody(equalToJson("{\"sortOrder\":\"asc\",\"sortBy\":\"startTime\","
            + "\"rootProcessInstances\":true}"))
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
            messageResolver.getMessage(ProcessInstanceStatusMessageTitle.CITIZEN_IN_PROGRESS))
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.ACTIVE);
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

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstanceResponse[].class);

    assertThat(result).hasSize(1);
    assertThat(result[0])
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinition1")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2020, 12, 1, 12, 0, 0, 0))
        .hasFieldOrPropertyWithValue("endTime", LocalDateTime.of(2020, 12, 2, 12, 0, 0, 0))
        .extracting(HistoryUserProcessInstanceResponse::getStatus)
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

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstanceResponse.class);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", "testId")
        .extracting(HistoryUserProcessInstanceResponse::getStatus)
        .hasFieldOrPropertyWithValue("code", UserProcessInstanceStatus.COMPLETED)
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

    var result = performForObjectAsOfficer(request, HistoryUserProcessInstanceResponse[].class);

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
