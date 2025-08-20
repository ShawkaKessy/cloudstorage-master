package ru.netology.cloudstorage.service;

import ru.netology.cloudstorage.entity.FileEntity;
import ru.netology.cloudstorage.entity.User;

import java.util.List;

public interface FileService {
    void uploadFile(User owner, String filename, byte[] content);
    byte[] downloadFile(User owner, String filename);
    void deleteFile(User owner, String filename);
    void renameFile(User owner, String oldName, String newName);
    List<FileEntity> listFiles(User owner);
}
