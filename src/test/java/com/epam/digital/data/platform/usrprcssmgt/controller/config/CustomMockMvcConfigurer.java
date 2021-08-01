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
