package com.example.processorder.domain.order.repository;

import com.example.processorder.domain.common.entity.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {
    List<QueueEntry> findByQueueIdAndIsActiveOrderByPositionAsc(Long queueId, Boolean isActive);
    Optional<QueueEntry> findByOrderIdAndIsActive(Long orderId, Boolean isActive);

    @Query("SELECT COUNT(qe) FROM QueueEntry qe WHERE qe.queue.id = :queueId AND qe.isActive = true")
    Long countActiveEntriesInQueue(@Param("queueId") Long queueId);
}

