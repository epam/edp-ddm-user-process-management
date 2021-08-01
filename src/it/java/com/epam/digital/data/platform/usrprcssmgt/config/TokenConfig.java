package com.epam.digital.data.platform.usrprcssmgt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("token")
@Data
public class TokenConfig {

  private String name;
  private String valueWithRoleOfficer;
  private String valueWithRoleCitizen;
}
