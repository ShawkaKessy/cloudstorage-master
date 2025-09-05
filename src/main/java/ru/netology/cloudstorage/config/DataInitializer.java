package ru.netology.cloudstorage.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.netology.cloudstorage.entity.FileEntity;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.entity.AuthToken;
import ru.netology.cloudstorage.repository.FileRepository;
import ru.netology.cloudstorage.repository.UserRepository;
import ru.netology.cloudstorage.repository.AuthTokenRepository;
import ru.netology.cloudstorage.util.PasswordUtil;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final FileRepository fileRepository;

    @PostConstruct
    public void init() {
        User user = new User("testuser@example.com", PasswordUtil.hash("123456"));
        userRepository.save(user);

        AuthToken token1 = new AuthToken();
        token1.setUser(user);
        token1.setToken("sample-token-1");
        authTokenRepository.save(token1);

        AuthToken token2 = new AuthToken();
        token2.setUser(user);
        token2.setToken("sample-token-2");
        authTokenRepository.save(token2);

        createFile(user, "document.pdf", "Это пример PDF файла.".getBytes());
        createFile(user, "photo.png", generateSampleImageBytes());
        createFile(user, "notes.txt", "Пример заметок для теста.".getBytes());
    }

    private void createFile(User user, String filename, byte[] content) {
        FileEntity file = new FileEntity();
        file.setOwner(user);
        file.setFilename(filename);
        file.setContent(content);
        fileRepository.save(file);
    }

    private byte[] generateSampleImageBytes() {
        byte[] image = new byte[1024]; // 1 KB
        for (int i = 0; i < image.length; i++) {
            image[i] = (byte) (i % 256);
        }
        return image;
    }
}
