package com.poc.graphql.service;

import java.util.List;

import com.poc.graphql.dto.OrderInput;
import com.poc.graphql.entity.Order;

public interface OrderService {

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    Order createOrder(OrderInput input);

    Order updateOrder(Long id, OrderInput input);

    void deleteOrder(Long id);
}
