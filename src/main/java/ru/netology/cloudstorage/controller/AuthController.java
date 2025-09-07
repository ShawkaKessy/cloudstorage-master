package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudstorage.dto.LoginRequest;
import ru.netology.cloudstorage.dto.LoginResponse;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.service.AuthService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.login(), request.password());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (UnauthorizedException ex) {
            // Возвращаем ошибки в формате, ожидаемом фронтом
            Map<String, String[]> errors = Map.of(
                    "email", new String[]{"Неверная почта или пароль"},
                    "password", new String[]{""}
            );
            return ResponseEntity.status(401).body(errors);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
