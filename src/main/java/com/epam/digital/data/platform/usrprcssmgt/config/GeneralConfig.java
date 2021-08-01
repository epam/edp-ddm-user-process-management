package com.epam.digital.data.platform.usrprcssmgt.config;

import com.epam.digital.data.platform.integration.ceph.config.CephConfig;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * The class represents a holder for beans of the general configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@Import(CephConfig.class)
@EnableAspectJAutoProxy
public class GeneralConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.simpleDateFormat(DATE_TIME_FORMAT)
            .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
            .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    }
}
