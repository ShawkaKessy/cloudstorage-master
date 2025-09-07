package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudstorage.dto.FileResponse;
import ru.netology.cloudstorage.dto.RenameFileRequest;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.service.AuthService;
import ru.netology.cloudstorage.service.FileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final AuthService authService;

    private User auth(String token) {
        return authService.getUserByToken(token);
    }

    @PostMapping("/file")
    public ResponseEntity<Void> uploadFile(@RequestHeader("auth-token") String token,
                                           @RequestParam String filename,
                                           @RequestParam("file") MultipartFile file) throws Exception {
        fileService.uploadFile(auth(token), filename, file.getBytes());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(@RequestHeader("auth-token") String token,
                                               @RequestParam String filename) {
        byte[] content = fileService.downloadFile(auth(token), filename);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestHeader("auth-token") String token,
                                           @RequestParam String filename) {
        fileService.deleteFile(auth(token), filename);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/file")
    public ResponseEntity<Void> renameFile(@RequestHeader("auth-token") String token,
                                           @RequestBody RenameFileRequest request) {
        User user = auth(token);
        fileService.renameFile(user, request.oldFilename(), request.newFilename());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<FileResponse> listFiles(@RequestHeader("auth-token") String token,
                                        @RequestParam(required = false) Integer limit) {
        User user = auth(token);
        var files = fileService.listFiles(user);
        if (limit != null && limit > 0) {
            files = files.stream().limit(limit).toList();
        }
        return files.stream()
                .map(f -> new FileResponse(f.getFilename(), f.getContent().length))
                .toList();
    }
}
