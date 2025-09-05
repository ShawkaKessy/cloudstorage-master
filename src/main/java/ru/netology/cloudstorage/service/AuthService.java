package ru.netology.cloudstorage.service;

import ru.netology.cloudstorage.entity.User;

public interface AuthService {
    String login(String email, String password);
    void logout(String token);
    User getUserByToken(String token);
}
