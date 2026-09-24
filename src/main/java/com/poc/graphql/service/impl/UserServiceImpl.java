package com.poc.graphql.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.poc.graphql.dto.UserInput;
import com.poc.graphql.entity.User;
import com.poc.graphql.exception.DuplicateResourceException;
import com.poc.graphql.exception.ResourceNotFoundException;
import com.poc.graphql.repository.UserRepository;
import com.poc.graphql.service.FileStorageService;
import com.poc.graphql.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserServiceImpl(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
        User user = getUserById(id);
        userRepository.delete(user);
        fileStorageService.delete(user.getProfileImageUrl());
    }

    @Override
    public User updateProfileImage(Long id, MultipartFile file) {
        User user = getUserById(id);
        String oldUrl = user.getProfileImageUrl();
        user.setProfileImageUrl(fileStorageService.storeProfileImage(file));
        User saved = userRepository.save(user);
        fileStorageService.delete(oldUrl);
        return saved;
    }

    @Override
    public void removeProfileImage(Long id) {
        User user = getUserById(id);
        String oldUrl = user.getProfileImageUrl();
        user.setProfileImageUrl(null);
        userRepository.save(user);
        fileStorageService.delete(oldUrl);
    }
}
