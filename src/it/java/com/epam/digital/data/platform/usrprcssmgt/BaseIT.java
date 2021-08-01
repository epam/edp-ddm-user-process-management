package com.epam.digital.data.platform.usrprcssmgt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.config.TokenConfig;
import com.epam.digital.data.platform.usrprcssmgt.util.CephKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
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
  @Autowired
  protected CephKeyProvider cephKeyProvider;

  @BeforeClass
  public static void setUpClass() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @After
  public void tearDown() {
    bpmServer.resetAll();
    cephServer.resetAll();
  }

  @SneakyThrows
  protected <T> T performForObject(MockHttpServletRequestBuilder request,
      Function<MockHttpServletRequestBuilder, ResultActions> performFunction, Class<T> tClass) {
    var json = performFunction.apply(request)
        .andExpect(status().isOk())
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
    mockGetBucket();
    cephServer.addStubMapping(
        stubFor(put(urlMatching(String.format("/%s/lowcode_.*_start_form_.*", cephBucketName)))
            .withRequestBody(containing(body))
            .willReturn(aResponse()
                .withStatus(200))));
  }

  private void mockGetBucket() {
    cephServer.addStubMapping(stubFor(get(urlPathEqualTo("/")).willReturn(
        aResponse()
            .withStatus(200)
            .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                + "</ListAllMyBucketsResult>"))));
  }

  @SneakyThrows
  public void mockValidationFormData(int status, String reqBody, String respBody) {
    formProviderServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/formKey/submission"))
            .withRequestBody(equalTo(reqBody))
            .withQueryParam("dryrun", equalTo("1"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(status)
                .withBody(respBody)
            )
        ));
  }
}
