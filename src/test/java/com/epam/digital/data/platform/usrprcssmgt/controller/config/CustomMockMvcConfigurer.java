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

package com.epam.digital.data.platform.usrprcssmgt.controller.config;

import com.epam.digital.data.platform.usrprcssmgt.config.GeneralConfig;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

public class CustomMockMvcConfigurer implements MockMvcConfigurer {

  @Override
  public void afterConfigurerAdded(@NonNull ConfigurableMockMvcBuilder<?> builder) {
    var jacksonBuilder = Jackson2ObjectMapperBuilder.json();
    new GeneralConfig().jackson2ObjectMapperBuilderCustomizer().customize(jacksonBuilder);

    ((StandaloneMockMvcBuilder) builder).setMessageConverters(
        new MappingJackson2HttpMessageConverter(jacksonBuilder.build()));
  }
}
