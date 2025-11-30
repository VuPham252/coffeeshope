package com.example.processorder.domain.order.dto;

import com.example.processorder.domain.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long coffeeShopId;
    private String coffeeShopName;
    private Long queueId;
    private String queueName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Integer estimatedWaitTimeMinutes;
    private Integer queuePosition;
    private String notes;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}

