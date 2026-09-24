package com.poc.graphql.dto;

public class ProfileImageResponse {

    private Long userId;
    private String contentType;
    private long sizeInBytes;

    public ProfileImageResponse() {
    }

    public ProfileImageResponse(Long userId, String contentType, long sizeInBytes) {
        this.userId = userId;
        this.contentType = contentType;
        this.sizeInBytes = sizeInBytes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
