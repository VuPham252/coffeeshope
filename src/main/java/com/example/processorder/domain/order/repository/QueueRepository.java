package com.example.processorder.domain.order.repository;

import com.example.processorder.domain.common.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByCoffeeShopIdAndIsActive(Long coffeeShopId, Boolean isActive);
}

