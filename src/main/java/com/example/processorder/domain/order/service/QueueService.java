package com.example.processorder.domain.order.service;

import com.example.processorder.domain.common.entity.CoffeeShop;
import com.example.processorder.domain.common.entity.Order;
import com.example.processorder.domain.common.entity.Queue;
import com.example.processorder.domain.common.entity.QueueEntry;
import com.example.processorder.domain.common.enums.OrderStatus;
import com.example.processorder.domain.common.exception.ResourceNotFoundException;
import com.example.processorder.domain.order.repository.QueueEntryRepository;
import com.example.processorder.domain.order.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {
    private final QueueEntryRepository queueEntryRepository;
    private final QueueRepository queueRepository;

    public void handleQueueOnOrderCancellation(Order order, Long orderId) {
        if (order.getQueue() != null && order.getStatus() == OrderStatus.IN_QUEUE) {
            QueueEntry queueEntry = queueEntryRepository.findByOrderIdAndIsActive(orderId, true)
                    .orElse(null);

            if (queueEntry != null) {
                queueEntry.setIsActive(false);
                queueEntry.setLeftAt(LocalDateTime.now());
                queueEntryRepository.save(queueEntry);

                Queue queue = queueRepository.findById(order.getQueue().getId()).orElse(null);
                if (queue != null && queue.getCurrentSize() > 0) {
                    queue.setCurrentSize(queue.getCurrentSize() - 1);
                    queueRepository.save(queue);

                    repositionQueue(queue.getId(), queueEntry.getPosition());
                }
            }
        }
    }

    public void handleQueueOnOrderCompletion(Order order, Long orderId) {
        QueueEntry queueEntry = queueEntryRepository.findByOrderIdAndIsActive(orderId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));

        queueEntry.setIsActive(false);
        queueEntry.setLeftAt(LocalDateTime.now());
        queueEntryRepository.save(queueEntry);

        Queue queue = queueRepository.findById(order.getQueue().getId()).orElse(null);
        if (queue != null && queue.getCurrentSize() > 0) {
            queue.setCurrentSize(queue.getCurrentSize() - 1);
            queueRepository.save(queue);
            repositionQueue(queue.getId(), queueEntry.getPosition());
        }
    }

    public void repositionQueue(Long queueId, int removedPosition) {
        List<QueueEntry> entries = queueEntryRepository.findByQueueIdAndIsActiveOrderByPositionAsc(queueId, true);

        int repositionedCount = 0;
        for (QueueEntry entry : entries) {
            if (entry.getPosition() > removedPosition) {
                entry.setPosition(entry.getPosition() - 1);
                CoffeeShop shop = entry.getOrder().getCoffeeShop();

                if (shop != null) {
                    entry.setEstimatedWaitTimeMinutes(entry.getPosition() * shop.getAverageProcessingTimeMinutes());
                }
                repositionedCount++;
            }
        }

        queueEntryRepository.saveAll(entries);
        log.info("Queue repositioned - Queue ID: {}, Repositioned entries: {}", queueId, repositionedCount);
    }

    public Queue getQueueById(Long queueId) {
        return queueRepository.findById(queueId).orElseThrow(() -> new ResourceNotFoundException("Queue not found"));
    }
}
