package com.example.processorder.domain.order.repository;

import com.example.processorder.domain.common.entity.CoffeeShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CoffeeShopRepository extends JpaRepository<CoffeeShop, Long> {
    List<CoffeeShop> findByIsActive(Boolean isActive);

    Page<CoffeeShop> findByIsActive(Boolean isActive, Pageable pageable);

    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(latitude)))) AS distance " +
            "FROM coffee_shop " +
            "WHERE is_active = true " +
            "ORDER BY distance " +
            "LIMIT :limit", nativeQuery = true)
    List<CoffeeShop> findNearestShops(@Param("latitude") BigDecimal latitude,
                                       @Param("longitude") BigDecimal longitude,
                                       @Param("limit") int limit);
}

