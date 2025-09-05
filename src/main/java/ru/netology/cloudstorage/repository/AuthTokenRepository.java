package ru.netology.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);
    void deleteByToken(String token);

    // добавляем метод для удаления токенов конкретного пользователя
    @Transactional
    void deleteByUser(User user);
}

