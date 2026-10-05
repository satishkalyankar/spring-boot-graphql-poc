package com.poc.graphql.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.poc.graphql.entity.QUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.poc.graphql.dto.UserInput;
import com.poc.graphql.entity.User;
import com.poc.graphql.exception.DuplicateResourceException;
import com.poc.graphql.exception.InvalidFileException;
import com.poc.graphql.exception.ResourceNotFoundException;
import com.poc.graphql.repository.UserRepository;
import com.poc.graphql.service.UserService;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    public UserServiceImpl(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        QUser user = QUser.user;
        return queryFactory.selectFrom(user).orderBy(user.id.asc()).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        QUser user = QUser.user;
        return Optional.ofNullable(queryFactory.selectFrom(user).where(user.id.eq(id)).fetchOne()).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String name, String email, int page, int size) {
        QUser user = QUser.user;
        JPAQuery<User> query = queryFactory.selectFrom(user);
        if (name != null && !name.isBlank()) {
            query = query.where(user.name.containsIgnoreCase(name));
        }
        if (email != null && !email.isBlank()) {
            query = query.where(user.email.containsIgnoreCase(email));
        }
        return query.orderBy(user.id.desc()).offset((long) page * size).limit(size).fetch();
    }

    @Override
    public User createUser(UserInput input) {
        if (isEmailTaken(input.getEmail(), null)) {
            throw new DuplicateResourceException("Email already exists: " + input.getEmail());
        }
        return userRepository.save(new User(input.getName(), input.getEmail()));
    }

    @Override
    public User updateUser(Long id, UserInput input) {
        User user = getUserById(id);
        if (isEmailTaken(input.getEmail(), id)) {
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

    private boolean isEmailTaken(String email, Long excludeId) {
        QUser user = QUser.user;
        BooleanExpression condition = user.email.eq(email);
        if (excludeId != null) {
            condition = condition.and(user.id.ne(excludeId));
        }
        return queryFactory.selectOne().from(user).where(condition).fetchFirst() != null;
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
}
