package ru.netology.cloudstorage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudstorage.dto.LoginRequest;
import ru.netology.cloudstorage.dto.RegisterRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("pass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", () -> true);
        // Если в application.yml включён context-path /cloud — оставляем как есть.
        // Тесты ниже используют абсолютные пути с /cloud.
    }

    @Autowired
    private MockMvc mockMvc;

    private static String token;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Order(1)
    void testRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "password");
        mockMvc.perform(post("/cloud/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void testLoginAndFileFlow() throws Exception {
        // Логин
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        var loginResult = mockMvc.perform(post("/cloud/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("auth-token").asText();

        // Загрузка файла (multipart)
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/cloud/file")
                        .file(file)
                        .param("filename", "file.txt")
                        .header("auth-token", token))
                .andExpect(status().isOk());

        // Скачивание
        mockMvc.perform(get("/cloud/file")
                        .header("auth-token", token)
                        .param("filename", "file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().bytes("Hello World".getBytes()));

        // ✅ Переименование: ?filename=old + body {"name":"new"}
        mockMvc.perform(put("/cloud/file")
                        .header("auth-token", token)
                        .param("filename", "file.txt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"file2.txt\"}"))
                .andExpect(status().isOk());

        // Список
        mockMvc.perform(get("/cloud/list")
                        .header("auth-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("file2.txt"));

        // Удаление
        mockMvc.perform(delete("/cloud/file")
                        .header("auth-token", token)
                        .param("filename", "file2.txt"))
                .andExpect(status().isOk());
    }
}
