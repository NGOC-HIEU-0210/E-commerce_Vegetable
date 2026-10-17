package com.hieu.service;

import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.request.CheckoutRequest;
import com.hieu.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    List<OrderDto> findAll();

    PageResponse<OrderDto> findPaged(Pageable pageable);

    OrderDto findById(Long id);

    OrderDto findByCode(String orderCode);

    List<OrderDto> findByUser(Long userId);

    PageResponse<OrderDto> search(Long statusId, String keyword, Pageable pageable);

    OrderDto checkout(CheckoutRequest request);

    OrderDto updateStatus(Long id, Long statusId);

    void delete(Long id);
}
