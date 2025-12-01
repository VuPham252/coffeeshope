package com.example.processorder.domain.order.service;

import com.example.processorder.domain.auth.repository.CustomerProfileRepository;
import com.example.processorder.domain.auth.repository.UserRepository;
import com.example.processorder.domain.common.entity.*;
import com.example.processorder.domain.order.dto.*;
import com.example.processorder.domain.order.repository.*;
import com.example.processorder.domain.common.enums.OrderStatus;
import com.example.processorder.domain.common.exception.BusinessException;
import com.example.processorder.domain.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    private final CoffeeShopRepository coffeeShopRepository;

    private final MenuItemRepository menuItemRepository;

    private final QueueRepository queueRepository;

    private final QueueEntryRepository queueEntryRepository;

    private final CustomerProfileRepository customerProfileRepository;

    private final QueueService queueService;

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        User user = getCustomer(customerId);
        CoffeeShop shop = getCoffeeShop(request.getCoffeeShopId());
        CustomerProfile profile = getCustomerProfile(user.getId());

        Queue queue = resolveQueue(request.getQueueId(), shop);

        List<OrderItem> orderItems = buildOrderItems(request.getItems(), shop);
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        Order order = createOrder(user, shop, queue, request.getNotes(), totalAmount, orderItems);

        int position = assignQueueEntry(queue, order, shop);
        log.info("Queue entry assigned - Position: {}, Queue ID: {}", position, queue.getId());

        updateCustomerProfile(profile);

        return mapToOrderResponse(order, user, shop, queue, position);
    }

    @Transactional
    public OrderResponse cancelOrder(Long customerId, Long orderId) {
        Order order = fetchAndValidateOrder(orderId, customerId);

        queueService.handleQueueOnOrderCancellation(order, orderId);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order cancelled successfully - Order ID: {}, Order Number: {}", orderId, order.getOrderNumber());

        User user = getCustomer(customerId);
        CoffeeShop shop = getCoffeeShop(order.getCoffeeShop().getId());

        return mapToOrderResponse(order, user, shop, null, null);
    }

    public QueuePositionResponse getQueuePosition(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(customerId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        if (order.getQueue() == null) {
            throw new BusinessException("Order is not in a queue");
        }

        QueueEntry queueEntry = queueEntryRepository.findByOrderIdAndIsActive(orderId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));

        Queue queue = queueRepository.findById(order.getQueue().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found"));

        Long totalInQueue = queueEntryRepository.countActiveEntriesInQueue(queue.getId());

        log.debug("Queue position retrieved - Order: {}, Position: {}/{}",
                  order.getOrderNumber(), queueEntry.getPosition(), totalInQueue);

        return QueuePositionResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .queueId(queue.getId())
                .queueName(queue.getQueueName())
                .position(queueEntry.getPosition())
                .totalInQueue(totalInQueue.intValue())
                .estimatedWaitTimeMinutes(queueEntry.getEstimatedWaitTimeMinutes())
                .status(order.getStatus().name())
                .build();
    }

    public List<OrderResponse> getCustomerOrders(Long customerId) {
        User user = getCustomer(customerId);
        List<Order> orders = orderRepository.findByUserId(customerId);

        return orders.stream()
                .map(order -> mapOrderWithDetails(order, user))
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getShopOrders(Long customerId, Long shopId) {
        List<Order> orders = orderRepository.findByUserIdAndCoffeeShopId(customerId, shopId);

        return orders.stream()
                .map(order -> {
                    User user = order.getUser();
                    return mapOrderWithDetails(order, user);
                })
                .sorted(Comparator.comparing(OrderResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(customerId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        User user = getCustomer(customerId);
        return mapOrderWithDetails(order, user);
    }

    @Transactional
    public OrderResponse serveCustomer(Long orderId) {
        Order order = fetchAndValidateOrder(orderId);

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        queueService.handleQueueOnOrderCompletion(order, orderId);

        incrementCustomerLoyalty(order.getUser().getId());

        log.info("Order completed successfully - Order ID: {}, Order Number: {}, Customer: {}",
                 orderId, order.getOrderNumber(), order.getUser().getName());

        User user = getCustomer(order.getUser().getId());
        CoffeeShop shop = getCoffeeShop(order.getCoffeeShop().getId());

        return mapToOrderResponse(order, user, shop, order.getQueue(), null);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order, User user, CoffeeShop shop, Queue queue, Integer position) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> {
                    MenuItem menuItem = item.getMenuItem();
                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .menuItemId(menuItem != null ? menuItem.getId() : null)
                            .menuItemName(menuItem != null ? menuItem.getName() : "Unknown")
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .specialInstructions(item.getSpecialInstructions())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(user != null ? user.getId() : null)
                .customerName(user != null ? user.getName() : null)
                .coffeeShopId(shop != null ? shop.getId() : null)
                .coffeeShopName(shop != null ? shop.getName() : null)
                .queueId(queue != null ? queue.getId() : null)
                .queueName(queue != null ? queue.getQueueName() : null)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .estimatedWaitTimeMinutes(order.getEstimatedWaitTimeMinutes())
                .queuePosition(position)
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }


    private User getCustomer(Long customerId) {
        return userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private CoffeeShop getCoffeeShop(Long shopId) {
        CoffeeShop shop = coffeeShopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Coffee shop not found"));

        if (!shop.getIsActive()) {
            throw new BusinessException("Coffee shop is not active");
        }
        return shop;
    }

    private CustomerProfile getCustomerProfile(Long userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
    }

    private Queue resolveQueue(Long queueId, CoffeeShop shop) {

        if (queueId != null) {
            Queue queue = queueRepository.findById(queueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Queue not found"));

            if (!queue.getCoffeeShop().getId().equals(shop.getId())) {
                throw new BusinessException("Queue does not belong to the shop");
            }
            return queue;
        }

        List<Queue> queues = queueRepository.findByCoffeeShopIdAndIsActive(shop.getId(), true);

        if (queues.isEmpty()) {
            throw new BusinessException("No active queues available");
        }

        return queues.stream()
                .min(Comparator.comparing(Queue::getCurrentSize))
                .orElseThrow(() -> new BusinessException("Unable to assign queue"));
    }

    private List<OrderItem> buildOrderItems(List<OrderItemRequest> items, CoffeeShop shop) {

        List<OrderItem> results = new ArrayList<>();

        for (OrderItemRequest request : items) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

            if (!menuItem.getCoffeeShop().getId().equals(shop.getId())) {
                throw new BusinessException("Menu item does not belong to the shop");
            }

            if (!menuItem.getIsAvailable()) {
                throw new BusinessException("Menu item " + menuItem.getName() + " is not available");
            }

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

            results.add(OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .specialInstructions(request.getSpecialInstructions())
                    .build());
        }

        return results;
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrder(User user, CoffeeShop shop, Queue queue,
                              String notes, BigDecimal totalAmount, List<OrderItem> items) {

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .coffeeShop(shop)
                .queue(queue)
                .notes(notes)
                .status(OrderStatus.IN_QUEUE)
                .totalAmount(totalAmount)
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(items);
        order.setOrderItems(items);

        return order;
    }

    private int assignQueueEntry(Queue queue, Order order, CoffeeShop shop) {

        int position = queue.getCurrentSize() + 1;
        int waitTime = position * shop.getAverageProcessingTimeMinutes();

        QueueEntry entry = QueueEntry.builder()
                .queue(queue)
                .order(order)
                .user(order.getUser())
                .position(position)
                .estimatedWaitTimeMinutes(waitTime)
                .isActive(true)
                .build();

        queueEntryRepository.save(entry);

        queue.setCurrentSize(position);
        queueRepository.save(queue);

        order.setEstimatedWaitTimeMinutes(waitTime);
        orderRepository.save(order);

        return position;
    }

    private void updateCustomerProfile(CustomerProfile profile) {
        profile.setTotalOrders(profile.getTotalOrders() + 1);
        profile.setLoyaltyScore(profile.getLoyaltyScore() + 1);
        customerProfileRepository.save(profile);
    }

    private void validateQueueCapacity(Queue queue) {
        if (queue == null) {
            throw new ResourceNotFoundException("Queue not found");
        }

        if (queue.getCurrentSize() <= 0) {
            throw new BusinessException("Queue is already empty");
        }
    }

    private Order fetchAndValidateOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(customerId)) {
            throw new BusinessException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel order with status: " + order.getStatus());
        }

        return order;
    }

    private Order fetchAndValidateOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.IN_QUEUE) {
            throw new BusinessException("Order is not in queue or already served/cancelled");
        }

        return order;
    }

    private OrderResponse mapOrderWithDetails(Order order, User user) {
        CoffeeShop shop = getCoffeeShop(order.getCoffeeShop().getId());
        Queue queue = order.getQueue() != null ? queueService.getQueueById(order.getQueue().getId()) : null;

        Integer position = null;
        if (order.getQueue() != null && order.getStatus() == OrderStatus.IN_QUEUE) {
            QueueEntry entry = queueEntryRepository.findByOrderIdAndIsActive(order.getId(), true).orElse(null);
            if (entry != null) {
                position = entry.getPosition();
            }
        }

        return mapToOrderResponse(order, user, shop, queue, position);
    }

    private void incrementCustomerLoyalty(Long userId) {
        CustomerProfile profile = getCustomerProfile(userId);
        profile.setLoyaltyScore(profile.getLoyaltyScore() + 1);
        customerProfileRepository.save(profile);
    }
}

