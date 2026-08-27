package com.sightseer.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = {
                "app.jwt.secret=aW50ZWdyYXRpb24tdGVzdC1zZWNyZXQta2V5LTMyaGFzaA==",
                "app.jwt.expiration=PT1H"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

        static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
                        .withDatabaseName("sightseer_test")
                        .withUsername("sightseer_test")
                        .withPassword("sightseer_test");

        static {
                POSTGRES.start();
        }

        @DynamicPropertySource
        static void configurePostgres(
                        DynamicPropertyRegistry registry) {
                registry.add(
                                "spring.datasource.url",
                                POSTGRES::getJdbcUrl);
                registry.add(
                                "spring.datasource.username",
                                POSTGRES::getUsername);
                registry.add(
                                "spring.datasource.password",
                                POSTGRES::getPassword);
        }

        @Autowired
        protected MockMvc mockMvc;
}