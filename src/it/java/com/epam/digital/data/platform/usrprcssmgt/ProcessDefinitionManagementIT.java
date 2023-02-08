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

package com.epam.digital.data.platform.usrprcssmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GroupedProcessDefinitionResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.StubRequest;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

class ProcessDefinitionManagementIT extends BaseIT {

  @Test
  void countProcessDefinitions() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/process-definition/count"))
        .queryParams(Map.of("active", equalTo("true"),
            "latestVersion", equalTo("true"),
            "suspended", equalTo("false")))
        .status(200)
        .responseBody("{ \"count\": 7 }")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/process-definition/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, CountResultDto.class);

    assertThat(result.getCount()).isEqualTo(7L);
  }

  @Test
  void getProcessDefinitionByKey() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/extended/process-definition/key/processDefinitionKey"))
        .status(200)
        .responseBody("{ \"id\": \"id1\", \"name\":\"name1\", \"formKey\":\"testFormKey\" }")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/process-definition/processDefinitionKey")
        .accept(MediaType.APPLICATION_JSON_VALUE);

    var result = performForObjectAsOfficer(request, DdmProcessDefinitionDto.class);

    assertThat(result).isNotNull()
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("name", "name1")
        .hasFieldOrPropertyWithValue("formKey", "testFormKey");
  }

  @Test
  void getProcessDefinitions() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-definition"))
        .requestBody(equalToJson("{\"active\":false,\"latestVersion\":true,"
            + "\"suspended\":false,\"sortBy\":\"name\",\"sortOrder\":\"asc\","
            + "\"processDefinitionId\":null,\"processDefinitionIdIn\":null}"))
        .status(200)
        .responseBody(
            "[ { \"id\": \"id1\", \"name\":\"name1\", \"formKey\":\"testFormKey\" }, "
                + "{ \"id\": \"id2\", \"name\":\"name2\" }] ")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/process-definition?active=false")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, DdmProcessDefinitionDto[].class);

    assertThat(result).isNotNull().hasSize(2);
    assertThat(result[0])
        .hasFieldOrPropertyWithValue("id", "id1")
        .hasFieldOrPropertyWithValue("name", "name1")
        .hasFieldOrPropertyWithValue("formKey", "testFormKey");
    assertThat(result[1])
        .hasFieldOrPropertyWithValue("id", "id2")
        .hasFieldOrPropertyWithValue("name", "name2")
        .hasFieldOrPropertyWithValue("formKey", null);
  }

  @Test
  void startProcessInstance() {
    var processInstanceId = "processInstanceId";
    var processDefinitionId = "processDefinitionId";
    var processDefinitionKey = "testKey";

    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(
            urlPathEqualTo(String.format("/api/process-definition/key/%s/start", processDefinitionKey)))
        .status(200)
        .responseBody(String.format("{ \"id\":\"%s\", \"definitionId\":\"%s\", \"ended\":false }",
            processInstanceId, processDefinitionId))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
    var payload = "{\"data\":null,"
            + "\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";
    mockPutStartFormCephKey(payload);
    var request = post(String.format("/api/process-definition/%s/start", processDefinitionKey))
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType("application/json")
        .content(payload);
    var result = performForObjectAsOfficer(request, StartProcessInstanceResponse.class);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .ended(false)
        .build();
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void startProcessInstanceWithForm() {
    var processInstanceId = "processInstanceId";
    var processDefinitionId = "processDefinitionId";
    var processDefinitionKey = "testKey";

    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/extended/process-definition/key/testKey"))
        .status(200)
        .responseBody("{\"id\":\"processInstanceId\",\"name\":\"name1\",\"formKey\":\"formKey\"}")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(
            urlPathEqualTo(
                String.format("/api/process-definition/key/%s/start", processDefinitionKey)))
        .status(200)
        .responseBody(String.format("{ \"id\":\"%s\", \"definitionId\":\"%s\", \"ended\":false }",
            processInstanceId, processDefinitionId))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var payload = "{\"data\":{\"formData\":\"testData\"},"
        + "\"signature\":\"eSign\","
        + "\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";
    mockPutStartFormCephKey(payload);
    mockValidationFormData(200, payload);

    mockGetForm("{\"components\":[{\"key\":\"name\",\"type\":\"textfield\"},"
        + "{\"key\":\"createdDate\",\"type\":\"day\"}]}");

    var request =
        post(String.format("/api/process-definition/%s/start-with-form", processDefinitionKey))
            .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
            .content(payload);

    var result = performForObjectAsOfficer(request, StartProcessInstanceResponse.class);

    var expectedResponse = StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .ended(false)
        .build();
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void failedStartProcessInstanceWithForm() throws Exception {
    var processDefinitionKey = "testKey";
    var payload = "{\"data\":{\"formData\":\"testData\"},\"signature\":\"eSign\"}";

    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/api/extended/process-definition/key/testKey"))
        .status(200)
        .responseBody("{ \"id\": \"processInstanceId\", \"name\":\"name1\" }")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    mockPutStartFormCephKey(payload);

    var request =
        post(String.format("/api/process-definition/%s/start-with-form", processDefinitionKey))
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType("application/json")
            .content(payload);

    performWithTokenOfficerRole(request).andExpect(status().isBadRequest());
  }

  @Test
  void failedStartProcessInstanceWithFormInvalidFormData() {
    var processDefinitionId = "processDefinitionId";
    var processDefinitionKey = "testKey";

    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlEqualTo(
            String.format("/api/extended/process-definition/key/%s", processDefinitionKey)))
        .status(200)
        .responseBody(String.format(
            "{\"id\":\"%s\",\"name\":\"name1\",\"key\":\"testKey\",\"formKey\":\"formKey\"}",
            processDefinitionId))
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var errorValidationResponse = "{\"details\":{\"errors\":[{\"value\": \"123\",\"field\": "
        + "\"name\",\"message\": \"Field name is required\"},{\"value\": \"321\",\"field\": "
        + "\"createdDate\",\"message\": \"Field createdDate is required\"}]}}";
    mockValidationFormData(422, errorValidationResponse);

    mockGetForm("{\"components\":[{\"key\":\"name\",\"type\":\"textfield\"},"
        + "{\"key\":\"createdDate\",\"type\":\"day\"}]}");

    var payload = "{\"data\":{\"formData\":\"testData\"},"
        + "\"signature\":\"eSign\","
        + "\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";
    var request =
        post(String.format("/api/process-definition/%s/start-with-form", processDefinitionKey))
            .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
            .content(payload);

    var result = performForObjectAsOfficerAndExpect(request, ValidationErrorDto.class,
        status().is4xxClientError());

    assertThat(result.getDetails().getErrors()).hasSize(2);
    assertThat(result.getDetails().getErrors().get(0).getField()).isEqualTo("name");
    assertThat(result.getDetails().getErrors().get(1).getField()).isEqualTo("createdDate");
  }

  @Test
  void shouldReturnBadRequestWithBrokenInputJson() throws Exception {
    var request = post("/api/process-definition/someProcess/start-with-form")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"data\" : { \"}}");

    performWithTokenOfficerRole(request).andExpect(status().is(400));
  }

  @Test
  void getGroupedProcessDefinitions() {
    mockBpmsRequest(StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/api/extended/process-definition"))
        .requestBody(equalToJson("{\"active\":true,\"latestVersion\":true,"
            + "\"suspended\":false,\"sortBy\":\"name\",\"sortOrder\":\"asc\","
            + "\"processDefinitionId\":null,\"processDefinitionIdIn\":null}"))
        .status(200)
        .responseBody(
            "[ { \"id\": \"123\", \"key\": \"first-process-group\", \"name\":\"name1\" }, "
                + "{ \"id\": \"345\", \"key\": \"without-group-1\", \"name\":\"name2\" }, "
                + "{ \"id\": \"234\", \"key\": \"third-process-group\", \"name\":\"name3\" }, "
                + "{ \"id\": \"567\", \"key\": \"without-group-2\", \"name\":\"name4\" }, "
                + "{ \"id\": \"456\", \"key\": \"second-process-group\",  \"name\":\"name5\" }] ")
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var request = get("/api/grouped-process-definition?active=true&suspended=false")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObjectAsOfficer(request, GroupedProcessDefinitionResponse.class);

    assertThat(result).isNotNull();
    assertThat(result.getGroups()).hasSize(1);
    assertThat(result.getUngrouped()).hasSize(3);
    assertThat(result.getGroups().get(0))
        .hasFieldOrPropertyWithValue("name", "Test group name");
    assertThat(result.getGroups().get(0).getProcessDefinitions().get(0).getKey())
        .hasToString("first-process-group");
    assertThat(result.getGroups().get(0).getProcessDefinitions().get(1).getKey())
        .hasToString("third-process-group");
    assertThat(result.getUngrouped().get(0))
        .hasFieldOrPropertyWithValue("name", "name5");
    assertThat(result.getUngrouped().get(1))
        .hasFieldOrPropertyWithValue("name", "name2");
    assertThat(result.getUngrouped().get(2))
        .hasFieldOrPropertyWithValue("name", "name4");
  }
}