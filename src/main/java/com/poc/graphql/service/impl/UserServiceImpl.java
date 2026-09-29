package com.poc.graphql.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.poc.graphql.dto.UserInput;
import com.poc.graphql.entity.User;
import com.poc.graphql.exception.DuplicateResourceException;
import com.poc.graphql.exception.InvalidFileException;
import com.poc.graphql.exception.ResourceNotFoundException;
import com.poc.graphql.repository.UserRepository;
import com.poc.graphql.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User createUser(UserInput input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + input.getEmail());
        }
        return userRepository.save(new User(input.getName(), input.getEmail()));
    }

    @Override
    public User updateUser(Long id, UserInput input) {
        User user = getUserById(id);
        if (userRepository.existsByEmailAndIdNot(input.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + input.getEmail());
        }
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(getUserById(id));
    }

    @Override
    public User updateProfileImage(Long id, MultipartFile file) {
        User user = getUserById(id);
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG and WEBP images are allowed");
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            user.setProfileImage("data:" + contentType + ";base64," + base64);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }
        return userRepository.save(user);
    }

    @Override
    public void removeProfileImage(Long id) {
        User user = getUserById(id);
        user.setProfileImage(null);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User provisionSsoUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User(name, email)));
    }
}
