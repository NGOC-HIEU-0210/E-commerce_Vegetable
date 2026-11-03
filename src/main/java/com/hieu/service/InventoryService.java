package com.hieu.service;

import com.hieu.enums.BatchStatus;
import com.hieu.model.Product;
import com.hieu.model.ProductBatch;
import com.hieu.payload.dto.BatchDto;
import com.hieu.payload.dto.WasteReportDto;
import com.hieu.payload.request.BatchRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.ProductBatchRepository;
import com.hieu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductBatchRepository batchRepository;
    private final ProductRepository productRepository;

    @Transactional
    public BatchDto receive(BatchRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));

        LocalDate received = req.getReceivedDate() == null ? PricingService.today() : req.getReceivedDate();
        LocalDate expiry = req.getExpiryDate();
        if (expiry == null) {
            if (product.getShelfLifeDays() == null || product.getShelfLifeDays() <= 0) {
                throw new IllegalArgumentException(
                        "Product has no shelfLifeDays, please provide expiryDate for this batch");
            }
            expiry = received.plusDays(product.getShelfLifeDays());
        }
        if (expiry.isBefore(received)) {
            throw new IllegalArgumentException("expiryDate must not be before receivedDate");
        }

        ProductBatch batch = batchRepository.save(ProductBatch.builder()
                .product(product)
                .initialQuantity(req.getQuantity())
                .remainingQuantity(req.getQuantity())
                .costPrice(req.getCostPrice())
                .receivedDate(received)
                .expiryDate(expiry)
                .status(BatchStatus.ACTIVE)
                .build());
        productRepository.increaseStock(product.getId(), req.getQuantity());
        return toDto(batch);
    }


    @Transactional
    public void consume(Long productId, int qty) {
        if (!batchRepository.existsByProductId(productId)) {
            return;
        }
        int left = qty;
        for (ProductBatch b : batchRepository.findSellableForUpdate(productId, BatchStatus.ACTIVE, PricingService.today())) {
            int take = Math.min(left, b.getRemainingQuantity());
            b.setRemainingQuantity(b.getRemainingQuantity() - take);
            if (b.getRemainingQuantity() == 0) {
                b.setStatus(BatchStatus.SOLD_OUT);
            }
            left -= take;
            if (left == 0) break;
        }
        if (left > 0) {
            throw new IllegalArgumentException("Not enough fresh stock for product " + productId);
        }
    }

    @Transactional
    public void restore(Long productId, int qty) {
        int left = qty;
        for (ProductBatch b : batchRepository.findRestorable(productId, BatchStatus.EXPIRED, PricingService.today())) {
            int room = b.getInitialQuantity() - b.getRemainingQuantity();
            int put = Math.min(left, room);
            if (put <= 0) continue;
            b.setRemainingQuantity(b.getRemainingQuantity() + put);
            b.setStatus(BatchStatus.ACTIVE);
            left -= put;
            if (left == 0) break;
        }
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public int writeOffExpired() {
        List<ProductBatch> overdue = batchRepository.findOverdue(BatchStatus.ACTIVE, PricingService.today());
        for (ProductBatch b : overdue) {
            int wasted = b.getRemainingQuantity();
            b.setWastedQuantity((b.getWastedQuantity() == null ? 0 : b.getWastedQuantity()) + wasted);
            b.setRemainingQuantity(0);
            b.setStatus(BatchStatus.EXPIRED);
            productRepository.writeOffStock(b.getProduct().getId(), wasted);
        }
        if (!overdue.isEmpty()) {
            log.info("Wrote off {} expired batches", overdue.size());
        }
        return overdue.size();
    }

    @Transactional(readOnly = true)
    public PageResponse<BatchDto> findExpiring(int days, Pageable pageable) {
        LocalDate until = PricingService.today().plusDays(Math.max(days, 0));
        return PageResponse.of(batchRepository.findExpiring(BatchStatus.ACTIVE, until, pageable), this::toDto);
    }

    @Transactional(readOnly = true)
    public PageResponse<BatchDto> findByProduct(Long productId, Pageable pageable) {
        return PageResponse.of(batchRepository.findByProductIdOrderByExpiryDateAsc(productId, pageable), this::toDto);
    }

    @Transactional(readOnly = true)
    public WasteReportDto wasteReport(LocalDate from, LocalDate to) {
        Map<Long, WasteReportDto.Item> byProduct = new LinkedHashMap<>();
        long totalQty = 0;
        double totalCost = 0;
        for (ProductBatch b : batchRepository.findWasted(from, to)) {
            double cost = b.getWastedQuantity() * (b.getCostPrice() == null ? 0 : b.getCostPrice());
            WasteReportDto.Item item = byProduct.computeIfAbsent(b.getProduct().getId(), id ->
                    WasteReportDto.Item.builder().productId(id).productName(b.getProduct().getName()).build());
            item.setWastedQuantity(item.getWastedQuantity() + b.getWastedQuantity());
            item.setWastedCost(item.getWastedCost() + cost);
            totalQty += b.getWastedQuantity();
            totalCost += cost;
        }
        return WasteReportDto.builder().from(from).to(to)
                .totalWastedQuantity(totalQty).totalWastedCost(totalCost)
                .items(List.copyOf(byProduct.values())).build();
    }

    private BatchDto toDto(ProductBatch b) {
        return BatchDto.builder()
                .id(b.getId())
                .productId(b.getProduct().getId())
                .productName(b.getProduct().getName())
                .initialQuantity(b.getInitialQuantity())
                .remainingQuantity(b.getRemainingQuantity())
                .wastedQuantity(b.getWastedQuantity())
                .costPrice(b.getCostPrice())
                .receivedDate(b.getReceivedDate())
                .expiryDate(b.getExpiryDate())
                .daysLeft(b.getExpiryDate() == null ? null
                        : (int) ChronoUnit.DAYS.between(PricingService.today(), b.getExpiryDate()))
                .status(b.getStatus() == null ? null : b.getStatus().name())
                .build();
    }
}
