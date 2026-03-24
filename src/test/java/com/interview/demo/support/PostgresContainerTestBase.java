package com.interview.demo.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("resource")
public abstract class PostgresContainerTestBase {
  static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES =
        new PostgreSQLContainer<>("postgres:17.3-alpine")
            .withDatabaseName("demo_test")
            .withUsername("test")
            .withPassword("test");
    POSTGRES.start();
    Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::close));
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
  }
}
