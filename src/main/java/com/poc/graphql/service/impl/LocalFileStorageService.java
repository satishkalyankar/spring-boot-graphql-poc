package com.poc.graphql.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.poc.graphql.exception.InvalidFileException;
import com.poc.graphql.service.FileStorageService;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    public static final String URL_PREFIX = "/uploads/";
    private static final String PROFILE_IMAGE_FOLDER = "profile-images";

    // The stored extension comes from the content type, never from the client file name
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path rootDir;

    public LocalFileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidFileException("Only JPEG, PNG and WEBP images are allowed");
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = rootDir.resolve(PROFILE_IMAGE_FOLDER).resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file " + fileName, e);
        }
        return URL_PREFIX + PROFILE_IMAGE_FOLDER + "/" + fileName;
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        Path path = rootDir.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!path.startsWith(rootDir)) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}", path, e);
        }
    }

    public Path getRootDir() {
        return rootDir;
    }
}
