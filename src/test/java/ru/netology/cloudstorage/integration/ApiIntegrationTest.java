package ru.netology.cloudstorage.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.netology.cloudstorage.dto.LoginResponse;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.FileRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private FileRepository fileRepository;

    private String token;
    private String testEmail;
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setup() {
        // Очищаем базы
        authTokenRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();

        // Создаём пользователя
        testEmail = "user-" + UUID.randomUUID() + "@example.com";
        User user = new User(testEmail, PasswordUtil.hash(TEST_PASSWORD));
        userRepository.save(user);

        // Логинимся и получаем токен
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginJson = "{\"email\":\"" + testEmail + "\",\"password\":\"" + TEST_PASSWORD + "\"}";
        HttpEntity<String> request = new HttpEntity<>(loginJson, headers);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity("/login", request, LoginResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Логин не прошёл");

        token = response.getBody().authToken();
        assertNotNull(token, "Токен не получен");
    }

    private HttpHeaders authHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", token);
        headers.setContentType(contentType);
        return headers;
    }

    @Test
    @Order(1)
    void uploadFile() {
        String filename = "test.txt";
        byte[] content = "Hello world".getBytes();

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(content, authHeaders(MediaType.APPLICATION_OCTET_STREAM));
        ResponseEntity<Void> response = restTemplate.exchange("/upload/" + filename, HttpMethod.POST, requestEntity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(2)
    void downloadFile() {
        String filename = "download.txt";
        byte[] content = "Download test".getBytes();

        // Загружаем файл
        restTemplate.exchange("/upload/" + filename, HttpMethod.POST, new HttpEntity<>(content, authHeaders(MediaType.APPLICATION_OCTET_STREAM)), Void.class);

        // Скачиваем файл
        ResponseEntity<byte[]> response = restTemplate.exchange("/download/" + filename, HttpMethod.GET, new HttpEntity<>(authHeaders(MediaType.APPLICATION_OCTET_STREAM)), byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(content, response.getBody());
    }

    @Test
    @Order(3)
    void renameFile() {
        String oldName = "old.txt";
        String newName = "new.txt";
        byte[] content = "Rename test".getBytes();

        // Загружаем старый файл
        restTemplate.exchange("/upload/" + oldName, HttpMethod.POST, new HttpEntity<>(content, authHeaders(MediaType.APPLICATION_OCTET_STREAM)), Void.class);

        // Переименовываем файл
        String renameJson = "{\"oldFilename\":\"" + oldName + "\",\"newFilename\":\"" + newName + "\"}";
        HttpEntity<String> renameRequest = new HttpEntity<>(renameJson, authHeaders(MediaType.APPLICATION_JSON));
        ResponseEntity<Void> renameResponse = restTemplate.exchange("/file", HttpMethod.PUT, renameRequest, Void.class);
        assertEquals(HttpStatus.OK, renameResponse.getStatusCode());

        // Проверяем новый файл
        ResponseEntity<byte[]> response = restTemplate.exchange("/download/" + newName, HttpMethod.GET, new HttpEntity<>(authHeaders(MediaType.APPLICATION_OCTET_STREAM)), byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(content, response.getBody());
    }

    @Test
    @Order(4)
    void listFiles() {
        String[] filenames = {"file1.txt", "file2.txt"};
        byte[] content = "List test".getBytes();

        for (String filename : filenames) {
            restTemplate.exchange("/upload/" + filename, HttpMethod.POST, new HttpEntity<>(content, authHeaders(MediaType.APPLICATION_OCTET_STREAM)), Void.class);
        }

        ResponseEntity<String> response = restTemplate.exchange("/list", HttpMethod.GET, new HttpEntity<>(authHeaders(MediaType.APPLICATION_JSON)), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (String filename : filenames) {
            assertTrue(response.getBody().contains(filename), "Список файлов должен содержать " + filename);
        }
    }
}
