package com.poc.graphql.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.poc.graphql.dto.UserInput;
import com.poc.graphql.entity.User;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(UserInput input);

    User updateUser(Long id, UserInput input);

    void deleteUser(Long id);

    User updateProfileImage(Long id, MultipartFile file);

    void removeProfileImage(Long id);

    User getUserByEmail(String email);

    // Returns the user for a SAML login, creating it on first sign-in
    User provisionSsoUser(String email, String name);
}
