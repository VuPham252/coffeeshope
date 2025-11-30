package com.example.processorder.domain.order.controller;

import com.example.processorder.domain.common.dto.ApiResponse;
import com.example.processorder.domain.order.dto.CreateOrderRequest;
import com.example.processorder.domain.order.dto.OrderResponse;
import com.example.processorder.domain.order.dto.QueuePositionResponse;
import com.example.processorder.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        Long customerId = (Long) authentication.getPrincipal();
        OrderResponse response = orderService.createOrder(customerId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllMyOrders(Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        List<OrderResponse> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getShopOrders(@PathVariable Long shopId, Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        List<OrderResponse> orders = orderService.getShopOrders(customerId, shopId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long customerId = (Long) authentication.getPrincipal();
        OrderResponse response = orderService.getOrderById(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long customerId = (Long) authentication.getPrincipal();
        OrderResponse response = orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    @GetMapping("/{orderId}/queue")
    public ResponseEntity<ApiResponse<QueuePositionResponse>> getQueuePosition(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long customerId = (Long) authentication.getPrincipal();
        QueuePositionResponse response = orderService.getQueuePosition(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{orderId}/serve")
    public ResponseEntity<ApiResponse<OrderResponse>> serveCustomer(
            @PathVariable Long orderId) {
        OrderResponse response = orderService.serveCustomer(orderId);
        return ResponseEntity.ok(ApiResponse.success("Customer served successfully", response));
    }
}
