package ru.netology.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.netology.cloudstorage.entity.FileEntity;
import ru.netology.cloudstorage.entity.User;
import ru.netology.cloudstorage.repository.FileRepository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    @Transactional
    public void uploadFile(User owner, String filename, byte[] content) {
        FileEntity file = new FileEntity();
        file.setOwner(owner);
        file.setFilename(filename);
        file.setContent(content);
        fileRepository.save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(User owner, String filename) {
        FileEntity file = fileRepository.findByOwnerAndFilename(owner, filename)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        return file.getContent();
    }

    @Override
    @Transactional
    public void deleteFile(User owner, String filename) {
        FileEntity file = fileRepository.findByOwnerAndFilename(owner, filename)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        fileRepository.delete(file);
    }

    @Override
    @Transactional
    public void renameFile(User owner, String oldName, String newName) {
        FileEntity file = fileRepository.findByOwnerAndFilename(owner, oldName)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        file.setFilename(newName);
        fileRepository.save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntity> listFiles(User owner) {
        return fileRepository.findAllByOwner(owner);
    }
}
