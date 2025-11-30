package com.example.processorder.domain.order.service;

import com.example.processorder.domain.order.dto.CoffeeShopResponse;
import com.example.processorder.domain.order.dto.MenuItemResponse;
import com.example.processorder.domain.order.dto.QueueResponse;
import com.example.processorder.domain.common.entity.CoffeeShop;
import com.example.processorder.domain.common.entity.MenuItem;
import com.example.processorder.domain.common.entity.Queue;
import com.example.processorder.domain.common.exception.ResourceNotFoundException;
import com.example.processorder.domain.order.repository.CoffeeShopRepository;
import com.example.processorder.domain.order.repository.MenuItemRepository;
import com.example.processorder.domain.order.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoffeeShopService {

    private final CoffeeShopRepository coffeeShopRepository;

    private final MenuItemRepository menuItemRepository;

    private final QueueRepository queueRepository;

    public List<CoffeeShopResponse> getAllActiveShops() {
        return coffeeShopRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CoffeeShopResponse getShopById(Long id) {
        CoffeeShop shop = coffeeShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coffee shop not found with id: " + id));
        return mapToResponse(shop);
    }

    public List<CoffeeShopResponse> getNearestShops(BigDecimal latitude, BigDecimal longitude, int limit) {
        return coffeeShopRepository.findNearestShops(latitude, longitude, limit).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getShopMenu(Long shopId) {
        if (!coffeeShopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException("Coffee shop not found with id: " + shopId);
        }
        return menuItemRepository.findByCoffeeShopIdAndIsAvailable(shopId, true).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    public List<QueueResponse> getShopQueues(Long shopId) {
        if (!coffeeShopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException("Coffee shop not found with id: " + shopId);
        }
        return queueRepository.findByCoffeeShopIdAndIsActive(shopId, true).stream()
                .map(this::mapToQueueResponse)
                .collect(Collectors.toList());
    }

    private CoffeeShopResponse mapToResponse(CoffeeShop shop) {
        return CoffeeShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .address(shop.getAddress())
                .contactNumber(shop.getContactNumber())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .numberOfQueues(shop.getNumberOfQueues())
                .maxQueueSize(shop.getMaxQueueSize())
                .openingTime(shop.getOpeningTime())
                .closingTime(shop.getClosingTime())
                .averageProcessingTimeMinutes(shop.getAverageProcessingTimeMinutes())
                .isActive(shop.getIsActive())
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .coffeeShopId(item.getCoffeeShop() != null ? item.getCoffeeShop().getId() : null)
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory() != null ? item.getCategory().getName() : null)
                .isAvailable(item.getIsAvailable())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .build();
    }

    private QueueResponse mapToQueueResponse(Queue queue) {
        return QueueResponse.builder()
                .id(queue.getId())
                .coffeeShopId(queue.getCoffeeShop() != null ? queue.getCoffeeShop().getId() : null)
                .queueNumber(queue.getQueueNumber())
                .queueName(queue.getQueueName())
                .maxSize(queue.getMaxSize())
                .currentSize(queue.getCurrentSize())
                .isActive(queue.getIsActive())
                .isFull(queue.getCurrentSize() >= queue.getMaxSize())
                .build();
    }
}

