package com.epam.digital.data.platform.usrprcssmgt;

import com.epam.digital.data.platform.bpms.client.config.FeignConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * The class represents a spring boot application runner that is used for running the application.
 */
@SpringBootApplication
@Import(FeignConfig.class)
@OpenAPIDefinition(info = @Info(title = "v1-alpha: User process management API",
    description = "All user process management operations"))
@EnableConfigurationProperties
public class UserProcessManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserProcessManagementApplication.class, args);
  }

}
