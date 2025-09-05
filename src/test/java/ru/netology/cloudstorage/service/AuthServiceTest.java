package ru.netology.cloudstorage.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.FileRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private String testEmail;
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();

        testEmail = "auth-" + UUID.randomUUID() + "@example.com";
        User user = new User(testEmail, PasswordUtil.hash(TEST_PASSWORD));
        userRepository.save(user);
    }

    @Test
    @Order(1)
    void loginGeneratesToken() {
        String token = authService.login(testEmail, TEST_PASSWORD);
        assertNotNull(token, "Токен не должен быть null");

        List<AuthToken> tokens = authTokenRepository.findAll();
        assertEquals(1, tokens.size(), "Должен быть один токен в базе");
        assertEquals(testEmail, tokens.get(0).getUser().getEmail());
        assertEquals(token, tokens.get(0).getToken());
    }

    @Test
    @Order(2)
    void loginWithWrongPasswordThrows() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authService.login(testEmail, "wrongpassword"));
        assertTrue(exception.getMessage().contains("Неверный пароль"));
    }

    @Test
    @Order(3)
    void logoutDeletesToken() {
        String token = authService.login(testEmail, TEST_PASSWORD);
        authService.logout(token);
        assertTrue(authTokenRepository.findByToken(token).isEmpty(), "Токен должен быть удалён после logout");
    }

    @Test
    @Order(4)
    void getUserByTokenReturnsCorrectUser() {
        String token = authService.login(testEmail, TEST_PASSWORD);
        User user = authService.getUserByToken(token);
        assertEquals(testEmail, user.getEmail(), "Возвращённый пользователь должен совпадать с email");
    }

    @Test
    @Order(5)
    void getUserByInvalidTokenThrows() {
        assertThrows(UnauthorizedException.class,
                () -> authService.getUserByToken("invalid-token"), "Должно кидать UnauthorizedException для некорректного токена");
    }
}
