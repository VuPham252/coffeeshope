package com.example.processorder.domain.order.repository;

import com.example.processorder.domain.common.entity.Order;
import com.example.processorder.domain.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndCoffeeShopId(Long userId, Long coffeeShopId);
}

