package com.poc.graphql.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** Saves the image and returns its public URL, e.g. /uploads/profile-images/abc.png */
    String storeProfileImage(MultipartFile file);

    void delete(String url);
}
