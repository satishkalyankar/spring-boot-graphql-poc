package com.poc.graphql.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poc.graphql.dto.OrderInput;
import com.poc.graphql.entity.Order;
import com.poc.graphql.exception.ResourceNotFoundException;
import com.poc.graphql.repository.OrderRepository;
import com.poc.graphql.service.OrderService;
import com.poc.graphql.service.UserService;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    public OrderServiceImpl(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        userService.getUserById(userId);
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order createOrder(OrderInput input) {
        return orderRepository.save(new Order(input.getProduct(), input.getAmount(), userService.getUserById(input.getUserId())));
    }

    @Override
    public Order updateOrder(Long id, OrderInput input) {
        Order order = getOrderById(id);
        order.setProduct(input.getProduct());
        order.setAmount(input.getAmount());
        order.setUser(userService.getUserById(input.getUserId()));
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.delete(getOrderById(id));
    }
}
