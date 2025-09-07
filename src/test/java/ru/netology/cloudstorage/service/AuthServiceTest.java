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
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        assertNotNull(token);

        List<AuthToken> tokens = authTokenRepository.findAll();
        assertEquals(1, tokens.size());
        assertEquals(testEmail, tokens.get(0).getUser().getEmail());
        assertEquals(token, tokens.get(0).getToken());
    }

    @Test
    @Order(2)
    void loginWithWrongPasswordThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(testEmail, "wrong"));
        assertTrue(ex.getErrors().containsKey("password"));
        assertArrayEquals(new String[]{"Неверный пароль"}, ex.getErrors().get("password"));
    }

    @Test
    @Order(3)
    void loginWithUnknownEmailThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login("unknown@example.com", TEST_PASSWORD));
        assertTrue(ex.getErrors().containsKey("email"));
        assertArrayEquals(new String[]{"Пользователь не найден"}, ex.getErrors().get("email"));
    }

    @Test
    @Order(4)
    void logoutDeletesToken() {
        String token = authService.login(testEmail, TEST_PASSWORD);
        authService.logout(token);
        assertTrue(authTokenRepository.findByToken(token).isEmpty());
    }

    @Test
    @Order(5)
    void getUserByTokenReturnsUser() {
        String token = authService.login(testEmail, TEST_PASSWORD);
        User user = authService.getUserByToken(token);
        assertEquals(testEmail, user.getEmail());
    }

    @Test
    @Order(6)
    void getUserByInvalidTokenThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.getUserByToken("invalid"));
        assertTrue(ex.getErrors().containsKey("token"));
        assertArrayEquals(new String[]{"Неверный токен"}, ex.getErrors().get("token"));
    }
}
