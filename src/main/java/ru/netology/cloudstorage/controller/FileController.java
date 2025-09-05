package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/upload/{filename}")
    public ResponseEntity<Void> uploadFile(@RequestHeader("auth-token") String token,
                                           @PathVariable String filename,
                                           @RequestBody byte[] content) {
        fileService.uploadFile(auth(token), filename, content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@RequestHeader("auth-token") String token,
                                               @PathVariable String filename) {
        byte[] content = fileService.downloadFile(auth(token), filename);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Void> deleteFile(@RequestHeader("auth-token") String token,
                                           @PathVariable String filename) {
        fileService.deleteFile(auth(token), filename);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/file")
    public ResponseEntity<Void> renameFile(
            @RequestHeader("auth-token") String token,
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
        if (limit != null && limit > 0 && limit < files.size()) {
            files = files.subList(0, limit);
        }
        return files.stream()
                .map(file -> new FileResponse(file.getFilename(), file.getContent().length))
                .toList();
    }
}
