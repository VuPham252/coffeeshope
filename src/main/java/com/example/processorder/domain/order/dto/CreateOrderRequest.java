package com.example.processorder.domain.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Coffee shop ID is required")
    private Long coffeeShopId;

    private Long queueId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    private String notes;
}

