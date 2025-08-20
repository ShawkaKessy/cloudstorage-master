package ru.netology.cloudstorage.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("cloudstorage_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Test
    void testRegister() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"login\":\"testuser@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginAndFileFlow() throws Exception {
        // Сначала регистрируем пользователя
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"login\":\"fileuser@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"login\":\"fileuser@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());

    }
}
