package com.example.processorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeShopResponse {
    private Long id;
    private String name;
    private String address;
    private String contactNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer numberOfQueues;
    private Integer maxQueueSize;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer averageProcessingTimeMinutes;
    private Boolean isActive;
    private Double distanceKm;
}

