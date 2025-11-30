package com.example.processorder.domain.order.repository;

import com.example.processorder.domain.common.entity.Category;
import com.example.processorder.domain.common.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCoffeeShopId(Long coffeeShopId);
    List<MenuItem> findByCoffeeShopIdAndIsAvailable(Long coffeeShopId, Boolean isAvailable);
}

