package com.hieu.service;

import com.hieu.model.OrderStatus;
import com.hieu.repository.OrderStatusRepository;
import com.hieu.service.OrderStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @InjectMocks
    private OrderStatusService orderStatusService;

    @Test
    void findAll_returnsList() {
        when(orderStatusRepository.findAll()).thenReturn(List.of(
                OrderStatus.builder().id(1L).name("PENDING").description("Cho xu ly").build()));

        List<OrderStatus> result = orderStatusService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findById_returnsStatus() {
        when(orderStatusRepository.findById(2L)).thenReturn(Optional.of(
                OrderStatus.builder().id(2L).name("COMPLETED").description("Hoan thanh").build()));

        OrderStatus result = orderStatusService.findById(2L);

        assertEquals(2L, result.getId());
        assertEquals("COMPLETED", result.getName());
    }

    @Test
    void findById_whenMissing_throws() {
        when(orderStatusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderStatusService.findById(99L));
    }
}
