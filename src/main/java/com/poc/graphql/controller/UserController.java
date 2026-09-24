package com.poc.graphql.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.poc.graphql.dto.ProfileImageResponse;
import com.poc.graphql.dto.UserInput;
import com.poc.graphql.entity.Order;
import com.poc.graphql.entity.User;
import com.poc.graphql.response.ApiResponse;
import com.poc.graphql.service.OrderService;
import com.poc.graphql.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @QueryMapping
    public ApiResponse<List<User>> users() {
        return ApiResponse.success("Users fetched successfully", userService.getAllUsers());
    }

    @QueryMapping
    public ApiResponse<User> user(@Argument Long id) {
        return ApiResponse.success("User fetched successfully", userService.getUserById(id));
    }

    @MutationMapping
    public ApiResponse<User> createUser(@Argument @Valid UserInput input) {
        return ApiResponse.success("User created successfully", userService.createUser(input));
    }

    @MutationMapping
    public ApiResponse<User> updateUser(@Argument Long id, @Argument @Valid UserInput input) {
        return ApiResponse.success("User updated successfully", userService.updateUser(id, input));
    }

    @MutationMapping
    public ApiResponse<Void> deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully");
    }

    // Profile image upload uses REST multipart; the URL is readable via GraphQL (User.profileImageUrl)
    @PostMapping(path = "/api/users/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse<ProfileImageResponse> uploadProfileImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        User user = userService.updateProfileImage(id, file);
        return ApiResponse.success("Profile image uploaded successfully",
                new ProfileImageResponse(user.getId(), user.getProfileImageUrl()));
    }

    @DeleteMapping("/api/users/{id}/profile-image")
    @ResponseBody
    public ApiResponse<Void> removeProfileImage(@PathVariable Long id) {
        userService.removeProfileImage(id);
        return ApiResponse.success("Profile image removed successfully");
    }

    // Resolves User.orders with a query instead of touching the lazy collection
    @SchemaMapping(typeName = "User", field = "orders")
    public List<Order> orders(User user) {
        return orderService.getOrdersByUserId(user.getId());
    }
}
