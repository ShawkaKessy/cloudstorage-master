package ru.netology.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.entity.AuthToken;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    Optional<AuthToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM AuthToken a WHERE a.token = :token")
    void deleteByToken(String token);
}
