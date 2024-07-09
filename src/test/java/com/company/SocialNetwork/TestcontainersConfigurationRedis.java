package com.company.SocialNetwork;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfigurationRedis {

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        GenericContainer<?> selfGenericContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.6-alpine"));
        return selfGenericContainer.withExposedPorts(6379);
    }
}
