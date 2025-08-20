package ru.netology.cloudstorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setLogin("test");
        user.setPassword(PasswordUtil.hash("pass"));

        when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

        String token = authService.login("test", "pass");

        assertNotNull(token);
        verify(authTokenRepository).save(any(AuthToken.class));
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User();
        user.setLogin("test");
        user.setPassword(PasswordUtil.hash("pass"));

        when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.login("test", "wrongpass"));
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login("unknown", "pass"));
    }

    @Test
    void testGetUserByTokenSuccess() {
        User user = new User();
        user.setLogin("test");

        AuthToken authToken = new AuthToken();
        authToken.setToken("abc");
        authToken.setUser(user);

        when(authTokenRepository.findByToken("abc")).thenReturn(Optional.of(authToken));

        User result = authService.getUserByToken("abc");

        assertEquals("test", result.getLogin());
    }

    @Test
    void testGetUserByTokenNotFound() {
        when(authTokenRepository.findByToken("badtoken")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.getUserByToken("badtoken"));
    }

    @Test
    void testLogout() {
        String token = "abc";

        authService.logout(token);

        verify(authTokenRepository).deleteByToken(token);
    }
}
