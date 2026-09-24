package com.poc.graphql.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.poc.graphql.service.impl.LocalFileStorageService;

// Serves uploaded files, e.g. GET /uploads/profile-images/abc.png
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LocalFileStorageService fileStorageService;

    public WebConfig(LocalFileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(LocalFileStorageService.URL_PREFIX + "**")
                .addResourceLocations(fileStorageService.getRootDir().toUri().toString());
    }
}
