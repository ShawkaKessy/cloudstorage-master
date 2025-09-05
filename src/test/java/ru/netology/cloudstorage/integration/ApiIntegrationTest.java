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
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.FileRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

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

    @BeforeAll
    static void initAll() {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();

        testEmail = "user-" + UUID.randomUUID() + "@example.com";
        User user = new User(testEmail, PasswordUtil.hash(TEST_PASSWORD));
        userRepository.save(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginJson = "{\"email\":\"" + testEmail + "\",\"password\":\"" + TEST_PASSWORD + "\"}";
        HttpEntity<String> request = new HttpEntity<>(loginJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/login", request, String.class);
        token = response.getHeaders().getFirst("auth-token");
    }

    @Test
    @Order(1)
    void uploadFile() {
        String filename = "test.txt";
        byte[] content = "Hello world".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", token);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(content, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/upload/" + filename,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(2)
    void downloadFile() {
        String filename = "download.txt";
        byte[] content = "Download test".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", token);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        restTemplate.exchange(
                "/upload/" + filename,
                HttpMethod.POST,
                new HttpEntity<>(content, headers),
                Void.class
        );

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/download/" + filename,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new String(content), new String(response.getBody()));
    }

    @Test
    @Order(3)
    void renameFile() {
        String oldName = "old.txt";
        String newName = "new.txt";
        byte[] content = "Rename test".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", token);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        restTemplate.exchange(
                "/upload/" + oldName,
                HttpMethod.POST,
                new HttpEntity<>(content, headers),
                Void.class
        );

        String renameJson = "{\"oldFilename\":\"" + oldName + "\",\"newFilename\":\"" + newName + "\"}";
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                "/file",
                HttpMethod.PUT,
                new HttpEntity<>(renameJson, headers),
                Void.class
        );

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/download/" + newName,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new String(content), new String(response.getBody()));
    }

    @Test
    @Order(4)
    void listFiles() {
        String[] filenames = {"file1.txt", "file2.txt"};
        byte[] content = "List test".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", token);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        for (String filename : filenames) {
            restTemplate.exchange(
                    "/upload/" + filename,
                    HttpMethod.POST,
                    new HttpEntity<>(content, headers),
                    Void.class
            );
        }

        ResponseEntity<String> response = restTemplate.exchange(
                "/list",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (String filename : filenames) {
            assertTrue(response.getBody().contains(filename), "Список файлов должен содержать " + filename);
        }
    }
}
