package com.hieu.service;

import com.hieu.model.OrderStatus;
import com.hieu.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    public List<OrderStatus> findAll() {
        return orderStatusRepository.findAll();
    }

    public OrderStatus findById(Long id) {
        return orderStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order status not found: " + id));
    }

    public OrderStatus findByName(String name) {
        return orderStatusRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Order status not found: " + name));
    }
}
