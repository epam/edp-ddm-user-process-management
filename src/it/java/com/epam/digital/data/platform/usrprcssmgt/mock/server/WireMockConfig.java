package com.epam.digital.data.platform.usrprcssmgt.mock.server;

import com.epam.digital.data.platform.usrprcssmgt.util.WireMockUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockConfig {

  @Qualifier("bpms")
  @Bean(destroyMethod = "stop")
  public WireMockServer bpmsWireMock(@Value("${bpms.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Qualifier("ceph")
  @Bean(destroyMethod = "stop")
  public WireMockServer cephWireMock(@Value("${ceph.http-endpoint}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }

  @Qualifier("form-provider")
  @Bean(destroyMethod = "stop")
  public WireMockServer formProviderWireMock(@Value("${form-management-provider.url}") String urlStr)
      throws MalformedURLException {
    return WireMockUtil.createAndStartMockServerForUrl(urlStr);
  }
}
