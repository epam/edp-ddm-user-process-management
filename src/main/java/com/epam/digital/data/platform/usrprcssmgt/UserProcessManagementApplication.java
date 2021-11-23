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
