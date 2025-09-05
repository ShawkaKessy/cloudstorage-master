package ru.netology.cloudstorage.dto;

public record RenameFileRequest(String oldFilename, String newFilename) {
}
