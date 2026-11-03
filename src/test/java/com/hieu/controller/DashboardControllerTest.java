package com.hieu.controller;

import com.hieu.model.OrderStatus;
import com.hieu.repository.CouponRepository;
import com.hieu.repository.CategoryRepository;
import com.hieu.repository.OrderStatusRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.SaleOrderRepository;
import com.hieu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(dashboardController)
                .build();
    }

    @Test
    void stats_returnsDashboardOverview() throws Exception {

        // Arrange: Mock số lượng tổng quan
        when(userRepository.count()).thenReturn(5L);
        when(productRepository.count()).thenReturn(10L);
        when(categoryRepository.count()).thenReturn(2L);
        when(saleOrderRepository.count()).thenReturn(7L);
        when(couponRepository.count()).thenReturn(3L);

        // Arrange: Mock trạng thái đơn hàng
        OrderStatus completed = OrderStatus.builder()
                .id(2L)
                .name("COMPLETED")
                .description("Hoan thanh")
                .build();

        OrderStatus pending = OrderStatus.builder()
                .id(1L)
                .name("PENDING")
                .description("Cho xu ly")
                .build();

        OrderStatus cancelled = OrderStatus.builder()
                .id(3L)
                .name("CANCELLED")
                .description("Da huy")
                .build();

        when(orderStatusRepository.findByName("COMPLETED"))
                .thenReturn(Optional.of(completed));

        when(orderStatusRepository.findByName("PENDING"))
                .thenReturn(Optional.of(pending));

        when(orderStatusRepository.findByName("CANCELLED"))
                .thenReturn(Optional.of(cancelled));

        // Arrange: Mock doanh thu và số lượng đơn hàng theo trạng thái
        when(saleOrderRepository.sumTotalByStatusId(2L))
                .thenReturn(1500.0);

        when(saleOrderRepository.countByStatusId(1L))
                .thenReturn(2L);

        when(saleOrderRepository.countByStatusId(2L))
                .thenReturn(0L);

        when(saleOrderRepository.countByStatusId(3L))
                .thenReturn(1L);

        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(5))
                .andExpect(jsonPath("$.data.totalProducts").value(10))
                .andExpect(jsonPath("$.data.totalCategories").value(2))
                .andExpect(jsonPath("$.data.totalOrders").value(7))
                .andExpect(jsonPath("$.data.totalCoupons").value(3))
                .andExpect(jsonPath("$.data.totalRevenue").value(1500))
                .andExpect(jsonPath("$.data.pendingOrders").value(2))
                .andExpect(jsonPath("$.data.cancelledOrders").value(1));
    }
}