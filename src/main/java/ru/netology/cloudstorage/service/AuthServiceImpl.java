package ru.netology.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.util.PasswordUtil;
import ru.netology.cloudstorage.util.TokenUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    @Override
    @Transactional
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        Map.of("email", new String[]{"Пользователь не найден"})
                ));

        if (!user.getPassword().equals(PasswordUtil.hash(password))) {
            throw new UnauthorizedException(
                    Map.of("password", new String[]{"Неверный пароль"})
            );
        }

        authTokenRepository.deleteByUser(user);

        String token = TokenUtil.generateToken();
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUser(user);
        authTokenRepository.save(authToken);

        return token;
    }

    @Override
    @Transactional
    public void logout(String token) {
        authTokenRepository.deleteByToken(token);
    }

    @Override
    public User getUserByToken(String token) {
        return authTokenRepository.findByToken(token)
                .map(AuthToken::getUser)
                .orElseThrow(() -> new UnauthorizedException(
                        Map.of("token", new String[]{"Неверный токен"})
                ));
    }
}
