package ru.netology.cloudstorage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repository.AuthTokenRepository;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthTokenRepository authTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.equals("/login") || path.equals("/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenValue = request.getHeader("auth-token");
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new UnauthorizedException(
                    Map.of("token", new String[]{"Токен отсутствует"})
            );
        }

        User user = authTokenRepository.findByToken(tokenValue)
                .map(t -> t.getUser())
                .orElseThrow(() -> new UnauthorizedException(
                        Map.of("token", new String[]{"Неверный токен"})
                ));

        request.setAttribute("user", user);
        filterChain.doFilter(request, response);
    }
}
