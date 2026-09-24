package com.poc.graphql.dto;

public class ProfileImageResponse {

    private Long userId;
    private String profileImageUrl;

    public ProfileImageResponse() {
    }

    public ProfileImageResponse(Long userId, String profileImageUrl) {
        this.userId = userId;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
