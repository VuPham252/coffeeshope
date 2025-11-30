package com.example.processorder.domain.order.controller;

import com.example.processorder.domain.common.dto.ApiResponse;
import com.example.processorder.domain.order.dto.CoffeeShopResponse;
import com.example.processorder.domain.order.dto.MenuItemResponse;
import com.example.processorder.domain.order.dto.QueueResponse;
import com.example.processorder.domain.order.service.CoffeeShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class CoffeeShopController {

    private final CoffeeShopService coffeeShopService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CoffeeShopResponse>>> getAllShops() {
        List<CoffeeShopResponse> shops = coffeeShopService.getAllActiveShops();
        return ResponseEntity.ok(ApiResponse.success(shops));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoffeeShopResponse>> getShopById(@PathVariable Long id) {
        CoffeeShopResponse shop = coffeeShopService.getShopById(id);
        return ResponseEntity.ok(ApiResponse.success(shop));
    }

    @GetMapping("/nearest")
    public ResponseEntity<ApiResponse<List<CoffeeShopResponse>>> getNearestShops(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "3") int limit) {
        List<CoffeeShopResponse> shops = coffeeShopService.getNearestShops(latitude, longitude, limit);
        return ResponseEntity.ok(ApiResponse.success(shops));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getShopMenu(@PathVariable Long id) {
        List<MenuItemResponse> menu = coffeeShopService.getShopMenu(id);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    @GetMapping("/{id}/queues")
    public ResponseEntity<ApiResponse<List<QueueResponse>>> getShopQueues(@PathVariable Long id) {
        List<QueueResponse> queues = coffeeShopService.getShopQueues(id);
        return ResponseEntity.ok(ApiResponse.success(queues));
    }
}

