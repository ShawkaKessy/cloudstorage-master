package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudstorage.dto.*;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.service.AuthService;
import ru.netology.cloudstorage.util.PasswordUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cloud")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByLogin(request.login()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setLogin(request.login());
        user.setPassword(PasswordUtil.hash(request.password()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseWrapper> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.login(), request.password());

            User user = userRepository.findByLogin(request.login())
                    .orElseThrow();

            return ResponseEntity.ok(new LoginResponseWrapper(
                    token,
                    new String[]{ user.getLogin() }  // только email
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(new LoginResponseWrapper(
                    null,
                    new String[]{ ex.getMessage() }
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
