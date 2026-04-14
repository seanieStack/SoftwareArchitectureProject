package io.github.seaniestack.supportservice.clients;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CoreServiceClientProperties.class)
public class CoreServiceClientConfiguration {
}
