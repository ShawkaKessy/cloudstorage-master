package ru.netology.cloudstorage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repository.AuthTokenRepository;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthTokenRepository authTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = request.getHeader("auth-token");

        if (tokenValue != null && !tokenValue.isBlank()) {
            Optional<AuthToken> authTokenOpt = authTokenRepository.findByToken(tokenValue);

            if (authTokenOpt.isEmpty()) {
                throw new UnauthorizedException();
            }

            User user = authTokenOpt.get().getUser();
            request.setAttribute("user", user);
        }

        filterChain.doFilter(request, response);
    }
}
