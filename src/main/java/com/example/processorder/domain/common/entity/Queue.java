package com.example.processorder.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "queue", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shop_queue", columnNames = {"coffee_shop_id", "queue_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_shop_id", nullable = false)
    private CoffeeShop coffeeShop;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Column(name = "queue_name", nullable = false, length = 50)
    private String queueName;

    @Column(name = "max_size", nullable = false)
    @Builder.Default
    private Integer maxSize = 50;

    @Column(name = "current_size", nullable = false)
    @Builder.Default
    private Integer currentSize = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "queue")
    @Builder.Default
    private List<QueueEntry> queueEntries = new ArrayList<>();

    @OneToMany(mappedBy = "queue")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

