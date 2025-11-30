package com.example.processorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueuePositionResponse {
    private Long orderId;
    private String orderNumber;
    private Long queueId;
    private String queueName;
    private Integer position;
    private Integer totalInQueue;
    private Integer estimatedWaitTimeMinutes;
    private String status;
}

