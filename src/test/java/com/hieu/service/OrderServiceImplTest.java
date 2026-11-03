
package com.hieu.service;

import com.hieu.model.SaleOrder;
import com.hieu.model.User;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CartItemRepository;
import com.hieu.repository.CouponRepository;
import com.hieu.repository.OrderStatusRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.SaleOrderRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.impl.OrderServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void findPaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);

        when(saleOrderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(order()),
                        pageable,
                        1
                ));

        PageResponse<OrderDto> result = orderService.findPaged(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(5L, result.getContent().get(0).getId());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void findByUser_returnsListOfOrders() {
        when(saleOrderRepository.findByUserId(1L))
                .thenReturn(List.of(order()));

        List<OrderDto> result = orderService.findByUser(1L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    void findById_returnsOrder() {
        when(saleOrderRepository.findById(5L))
                .thenReturn(Optional.of(order()));

        OrderDto result = orderService.findById(5L);

        assertEquals(5L, result.getId());
        assertEquals("ORD1", result.getOrderCode());
    }

    @Test
    void checkout_throwsWhenItemsEmpty() {
        com.hieu.payload.request.CheckoutRequest request =
                new com.hieu.payload.request.CheckoutRequest();

        request.setItems(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
    }

    @Test
    void updateStatus_returnsUpdatedOrder() {
        SaleOrder saleOrder = order();

        when(saleOrderRepository.findById(5L))
                .thenReturn(Optional.of(saleOrder));

        when(orderStatusRepository.findById(2L))
                .thenReturn(Optional.of(
                        com.hieu.model.OrderStatus.builder()
                                .id(2L)
                                .name("COMPLETED")
                                .description("Hoan thanh")
                                .build()
                ));

        when(saleOrderRepository.save(any(SaleOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.updateStatus(5L, 2L);

        assertEquals(5L, result.getId());
        assertEquals("COMPLETED", saleOrder.getStatus().getName());

        verify(saleOrderRepository).save(saleOrder);
    }

    @Test
    void cancelByUser_cancelsPendingOrder() {
        SaleOrder saleOrder = order();

        when(saleOrderRepository.findById(5L))
                .thenReturn(Optional.of(saleOrder));

        when(orderStatusRepository.findByName("CANCELLED"))
                .thenReturn(Optional.of(
                        com.hieu.model.OrderStatus.builder()
                                .id(3L)
                                .name("CANCELLED")
                                .description("Da huy")
                                .build()
                ));

        when(saleOrderRepository.save(any(SaleOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.cancelByUser(5L, 1L);

        assertEquals(5L, result.getId());
        assertEquals("CANCELLED", saleOrder.getStatus().getName());

        verify(saleOrderRepository).save(saleOrder);
    }

    @Test
    void updatePaymentStatus_returnsUpdatedOrder() {
        SaleOrder saleOrder = order();

        when(saleOrderRepository.findById(5L))
                .thenReturn(Optional.of(saleOrder));

        when(saleOrderRepository.save(any(SaleOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.updatePaymentStatus(
                5L,
                com.hieu.enums.PaymentStatus.PAID
        );

        assertEquals(5L, result.getId());

        verify(saleOrderRepository).save(saleOrder);
    }

    @Test
    void delete_whenMissing_throws() {
        when(saleOrderRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> orderService.delete(99L)
        );

        verify(saleOrderRepository).existsById(99L);
        verify(saleOrderRepository, never()).deleteById(99L);
    }

    private SaleOrder order() {
        return SaleOrder.builder()
                .id(5L)
                .orderCode("ORD1")
                .customerName("John")
                .status(
                        com.hieu.model.OrderStatus.builder()
                                .id(1L)
                                .name("PENDING")
                                .description("Cho xu ly")
                                .build()
                )
                .user(
                        User.builder()
                                .id(1L)
                                .username("john")
                                .build()
                )
                .items(new ArrayList<>())
                .build();
    }
}
