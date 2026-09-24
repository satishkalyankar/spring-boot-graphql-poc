package com.poc.graphql.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.poc.graphql.dto.OrderInput;
import com.poc.graphql.entity.Order;
import com.poc.graphql.entity.User;
import com.poc.graphql.response.ApiResponse;
import com.poc.graphql.service.OrderService;
import com.poc.graphql.service.UserService;

import jakarta.validation.Valid;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @QueryMapping
    public ApiResponse<List<Order>> orders() {
        return ApiResponse.success("Orders fetched successfully", orderService.getAllOrders());
    }

    @QueryMapping
    public ApiResponse<Order> order(@Argument Long id) {
        return ApiResponse.success("Order fetched successfully", orderService.getOrderById(id));
    }

    @QueryMapping
    public ApiResponse<List<Order>> ordersByUser(@Argument Long userId) {
        return ApiResponse.success("Orders fetched successfully", orderService.getOrdersByUserId(userId));
    }

    @MutationMapping
    public ApiResponse<Order> createOrder(@Argument @Valid OrderInput input) {
        return ApiResponse.success("Order created successfully", orderService.createOrder(input));
    }

    @MutationMapping
    public ApiResponse<Order> updateOrder(@Argument Long id, @Argument @Valid OrderInput input) {
        return ApiResponse.success("Order updated successfully", orderService.updateOrder(id, input));
    }

    @MutationMapping
    public ApiResponse<Void> deleteOrder(@Argument Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("Order deleted successfully");
    }

    // Order.user is a lazy proxy; only its id is safe to read outside the transaction
    @SchemaMapping(typeName = "Order", field = "user")
    public User user(Order order) {
        return userService.getUserById(order.getUser().getId());
    }
}
