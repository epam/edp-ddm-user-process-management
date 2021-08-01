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
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class ProcessDefinitionManagementIT extends BaseIT {

  @Test
  public void countProcessDefinitions() {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/process-definition/count"))
        .withQueryParam("active", equalTo("true"))
        .withQueryParam("latestVersion", equalTo("true"))
        .withQueryParam("suspended", equalTo("false"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"count\": 7 }"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/process-definition/count")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole, CountResultDto.class);

    assertThat(result.getCount(), is(7L));
  }

  @Test
  public void getProcessDefinitionById() {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/process-definition/processDefinitionId"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"id\": \"id1\", \"name\":\"name1\" }"))));

    bpmServer.addStubMapping(stubFor(post(urlPathEqualTo("/api/extended/start-form"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"id1\": \"testFormKey\" }"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/process-definition/processDefinitionId")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        UserProcessDefinitionDto.class);

    assertNotNull(result);
    assertThat(result.getId(), is("id1"));
    assertThat(result.getName(), is("name1"));
    assertThat(result.getFormKey(), is("testFormKey"));
  }

  @Test
  public void getProcessDefinitions() {
    bpmServer.addStubMapping(stubFor(get(urlPathEqualTo("/api/process-definition"))
        .withQueryParam("active", equalTo("false"))
        .withQueryParam("latestVersion", equalTo("true"))
        .withQueryParam("suspended", equalTo("false"))
        .withQueryParam("sortBy", equalTo("name"))
        .withQueryParam("sortOrder", equalTo("asc"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("[ { \"id\": \"id1\", \"name\":\"name1\" }, { \"id\": \"id2\", \"name\":\"name2\" }] "))));

    bpmServer.addStubMapping(stubFor(post(urlPathEqualTo("/api/extended/start-form"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"id1\": \"testFormKey\" }"))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .get("/api/process-definition?active=false")
        .accept(MediaType.APPLICATION_JSON_VALUE);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        UserProcessDefinitionDto[].class);

    assertNotNull(result);
    assertThat(result.length, is(2));
    assertThat(result[0].getId(), is("id1"));
    assertThat(result[0].getName(), is("name1"));
    assertThat(result[0].getFormKey(), is("testFormKey"));
    assertThat(result[1].getId(), is("id2"));
    assertThat(result[1].getName(), is("name2"));
  }

  @Test
  public void startProcessInstance() {
    var processInstanceId = "processInstanceId";
    var processDefinitionId = "processDefinitionId";

    bpmServer.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/process-definition/%s", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    String.format("{ \"id\":\"%s\", \"name\":\"name1\" }", processDefinitionId)))));

    bpmServer.addStubMapping(stubFor(
        post(urlEqualTo(String.format("/api/process-definition/%s/start", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    String.format("{ \"id\":\"%s\", \"definitionId\":\"%s\", \"ended\":false }",
                        processInstanceId,
                        processDefinitionId)))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .post(String.format("/api/process-definition/%s/start", processDefinitionId))
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content("{}");
    var result = performForObject(request, this::performWithTokenOfficerRole,
        StartProcessInstanceResponse.class);

    assertThat(result, is(StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .ended(false).build()));
  }

  @Test
  public void failedStartProcessInstance() throws Exception {
    var processDefinitionId = "processDefinitionId";
    var errorDto = new SystemErrorDto();
    errorDto.setMessage("Not found");
    errorDto.setCode("404");

    bpmServer.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/process-definition/%s", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorDto)))));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .post(String.format("/api/process-definition/%s/start", processDefinitionId))
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content("{}");
    var result = performWithTokenOfficerRole(request).andExpect(status().isNotFound()).andReturn();

    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            SystemErrorDto.class);

    Assertions.assertThat(resultBody.getMessage()).isEqualTo("Not found");
  }

  @Test
  public void startProcessInstanceWithForm() {
    var processInstanceId = "processInstanceId";
    var processDefinitionId = "processDefinitionId";
    var payload = "{\"data\":{\"formData\":\"testData\"},\"signature\":\"eSign\",\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";

    bpmServer.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/process-definition/%s", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(String
                    .format("{ \"id\":\"%s\", \"name\":\"name1\", \"key\":\"testKey\"  }",
                        processDefinitionId)))));

    bpmServer.addStubMapping(stubFor(
        post(urlEqualTo(String.format("/api/process-definition/%s/start", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    String.format("{ \"id\":\"%s\", \"definitionId\":\"%s\", \"ended\":false }",
                        processInstanceId,
                        processDefinitionId)))));

    bpmServer.addStubMapping(stubFor(
        get(urlEqualTo(String.format("/api/process-definition/%s/startForm", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"key\":\"formKey\" }"))));

    mockPutStartFormCephKey(payload);
    mockValidationFormData(200, payload, payload);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .post(String.format("/api/process-definition/%s/start-with-form", processDefinitionId))
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content(payload);
    var result = performForObject(request, this::performWithTokenOfficerRole,
        StartProcessInstanceResponse.class);

    assertThat(result, is(StartProcessInstanceResponse.builder()
        .id(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .ended(false).build()));
  }

  @Test
  public void failedStartProcessInstanceWithForm() throws Exception {
    var processDefinitionId = "processDefinitionId";
    var payload = "{\"data\":{\"formData\":\"testData\"},\"signature\":\"eSign\"}";

    bpmServer.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/process-definition/%s", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(String
                    .format("{ \"id\":\"%s\", \"name\":\"name1\", \"key\":\"testKey\"  }",
                        processDefinitionId)))));

    bpmServer.addStubMapping(stubFor(
        get(urlEqualTo(String.format("/api/process-definition/%s/startForm", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"key\": null }"))));

    mockPutStartFormCephKey(payload);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .post(String.format("/api/process-definition/%s/start-with-form", processDefinitionId))
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content(payload);

    performWithTokenOfficerRole(request).andExpect(status().isBadRequest());
  }

  @Test
  public void failedStartProcessInstanceWithFormInvalidFormData() throws Exception {
    var formDtoResponse = "{\"components\":[{\"key\":\"name\",\"type\":\"textfield\"},{\"key\":\"createdDate\","
        + "\"type\":\"day\"}]}";
    var processDefinitionId = "processDefinitionId";
    var payload = "{\"data\":{\"formData\":\"testData\"},\"signature\":\"eSign\",\"x-access-token\":\"" + tokenConfig.getValueWithRoleOfficer() + "\"}";
    var errorValidationResponse = "{\"details\": [{\"message\": \"Field name is required\","
        + "\"context\": {\"key\": \"name\",\"value\": \"123\" }}, {\"message\": \"Field name is required\","
        + "\"context\": {\"key\": \"createdDate\",\"value\": \"321\" }}]}";
    bpmServer.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/process-definition/%s", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(String
                    .format("{ \"id\":\"%s\", \"name\":\"name1\", \"key\":\"testKey\"  }",
                        processDefinitionId)))));

    bpmServer.addStubMapping(stubFor(
        get(urlEqualTo(String.format("/api/process-definition/%s/startForm", processDefinitionId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"key\":\"formKey\" }"))));

    formProviderServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/formKey"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(formDtoResponse)
            )
        ));

    mockValidationFormData(400, payload, errorValidationResponse);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
        .post(String.format("/api/process-definition/%s/start-with-form", processDefinitionId))
        .accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json").content(payload);

    var result = performWithTokenOfficerRole(request).andExpect(status().is4xxClientError()).andReturn();
    var resultBody = objectMapper
        .readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
            ValidationErrorDto.class);

    Assertions.assertThat(resultBody.getDetails().getErrors().size()).isEqualTo(1);
    Assertions.assertThat(resultBody.getDetails().getErrors().get(0).getField()).isEqualTo("name");
    Assertions.assertThat(resultBody.getDetails().getErrors().get(0).getValue()).isEqualTo("123");
  }
}