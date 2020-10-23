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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.config.TokenConfig;
import com.epam.digital.data.platform.usrprcssmgt.model.StubRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  @Autowired
  protected TokenConfig tokenConfig;
  @Autowired
  @Qualifier("form-provider")
  protected WireMockServer formProviderServer;
  @Autowired
  @Qualifier("bpms")
  protected WireMockServer bpmServer;
  @Autowired
  @Qualifier("ceph")
  protected WireMockServer cephServer;
  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected MessageResolver messageResolver;
  @Autowired
  protected ObjectMapper objectMapper;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @BeforeAll
  public static void setUpClass() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @AfterEach
  public void tearDown() {
    bpmServer.resetAll();
    cephServer.resetAll();
  }

  private void mockRequest(WireMockServer mockServer, StubRequest stubRequest) {
    var mappingBuilderMethod = getMappingBuilderMethod(stubRequest.getMethod());
    var mappingBuilder = mappingBuilderMethod.apply(stubRequest.getPath());
    stubRequest.getQueryParams().forEach(mappingBuilder::withQueryParam);
    stubRequest.getRequestHeaders().forEach(
        (header, values) -> values.forEach(value -> mappingBuilder.withHeader(header, value)));
    if (Objects.nonNull(stubRequest.getRequestBody())) {
      mappingBuilder.withRequestBody(stubRequest.getRequestBody());
    }

    var response = aResponse().withStatus(stubRequest.getStatus());
    stubRequest.getResponseHeaders()
        .forEach((header, values) -> response.withHeader(header, values.toArray(new String[0])));
    if (Objects.nonNull(stubRequest.getResponseBody())) {
      response.withBody(stubRequest.getResponseBody());
    }

    mockServer.addStubMapping(stubFor(mappingBuilder.willReturn(response)));
  }

  private Function<UrlPattern, MappingBuilder> getMappingBuilderMethod(HttpMethod method) {
    switch (method) {
      case GET:
        return WireMock::get;
      case PUT:
        return WireMock::put;
      case POST:
        return WireMock::post;
      case DELETE:
        return WireMock::delete;
      case HEAD:
        return WireMock::head;
      case OPTIONS:
        return WireMock::options;
      case PATCH:
        return WireMock::patch;
      case TRACE:
        return WireMock::trace;
      default:
        throw new IllegalStateException("All http methods are mapped with mapping builder");
    }
  }

  protected void mockBpmsRequest(StubRequest stubRequest) {
    mockRequest(bpmServer, stubRequest);
  }

  protected <T> T performForObjectAsCitizen(MockHttpServletRequestBuilder request,
      Class<T> tClass) {
    return performForObjectAsCitizenAndExpect(request, tClass, status().isOk());
  }

  protected <T> T performForObjectAsCitizenAndExpect(MockHttpServletRequestBuilder request,
      Class<T> tClass, ResultMatcher status) {
    return performForObject(performWithTokenCitizenRole(request), tClass, status);
  }

  protected <T> T performForObjectAsOfficer(MockHttpServletRequestBuilder request,
      Class<T> tClass) {
    return performForObjectAsOfficerAndExpect(request, tClass, status().isOk());
  }

  protected <T> T performForObjectAsOfficerAndExpect(MockHttpServletRequestBuilder request,
      Class<T> tClass, ResultMatcher status) {
    return performForObject(performWithTokenOfficerRole(request), tClass, status);
  }

  @SneakyThrows
  protected <T> T performForObject(ResultActions resultActions, Class<T> tClass,
      ResultMatcher status) {
    var json = resultActions
        .andExpect(status)
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    return objectMapper.readValue(json, tClass);
  }

  @SneakyThrows
  protected ResultActions performWithTokenOfficerRole(MockHttpServletRequestBuilder request) {
    return mockMvc
        .perform(request.header(tokenConfig.getName(), tokenConfig.getValueWithRoleOfficer()));
  }

  @SneakyThrows
  protected ResultActions performWithTokenCitizenRole(MockHttpServletRequestBuilder request) {
    return mockMvc
        .perform(request.header(tokenConfig.getName(), tokenConfig.getValueWithRoleCitizen()));
  }

  protected void mockPutStartFormCephKey(String body) {
    mockRequest(cephServer, StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/"))
        .status(200)
        .responseBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
            + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
            + "</ListAllMyBucketsResult>")
        .build());
    mockRequest(cephServer, StubRequest.builder()
        .method(HttpMethod.PUT)
        .path(urlMatching(
            String.format("/%s/process-definition/testKey/start-form/.*", cephBucketName)))
        .requestBody(containing(body))
        .status(200)
        .build());
  }

  @SneakyThrows
  public void mockValidationFormData(int status, String respBody) {
    mockRequest(formProviderServer, StubRequest.builder()
        .method(HttpMethod.POST)
        .path(urlPathEqualTo("/formKey/submission"))
        .queryParams(Map.of("dryrun", equalTo("1")))
        .requestBody(equalTo("{\"data\":{\"formData\":\"testData\"}}"))
        .status(status)
        .responseBody(respBody)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }

  @SneakyThrows
  public void mockGetForm(String respBody) {
    mockRequest(formProviderServer, StubRequest.builder()
        .method(HttpMethod.GET)
        .path(urlPathEqualTo("/formKey"))
        .status(200)
        .responseBody(respBody)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());
  }
}
