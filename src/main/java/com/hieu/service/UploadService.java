package com.hieu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${app.upload-dir:UploadFiles}")
    private String uploadDir;

    public String save(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null
                    ? "file" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot);
            }
            String filename = UUID.randomUUID() + ext;

            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            return "/UploadFiles/" + subFolder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }
}
