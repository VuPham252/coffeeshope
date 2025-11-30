package com.example.processorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueResponse {
    private Long id;
    private Long coffeeShopId;
    private Integer queueNumber;
    private String queueName;
    private Integer maxSize;
    private Integer currentSize;
    private Boolean isActive;
    private Boolean isFull;
}

