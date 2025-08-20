package ru.netology.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;
import ru.netology.cloudstorage.util.TokenUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    @Override
    public String login(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getPassword().equals(PasswordUtil.hash(password))) {
            throw new RuntimeException("Неверный пароль");
        }

        String token = TokenUtil.generateToken();

        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUser(user);

        authTokenRepository.save(authToken);

        return token;
    }

    @Override
    public void logout(String token) {
        authTokenRepository.deleteByToken(token);
    }

    @Override
    public User getUserByToken(String token) {
        return authTokenRepository.findByToken(token)
                .map(AuthToken::getUser)
                .orElseThrow(() -> new RuntimeException("Неверный токен"));
    }
}
