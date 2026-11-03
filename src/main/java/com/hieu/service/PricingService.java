package com.hieu.service;

import com.hieu.enums.BatchStatus;
import com.hieu.model.Product;
import com.hieu.repository.ProductBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class PricingService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProductBatchRepository batchRepository;

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public Map<Long, LocalDate> earliestExpiry(Collection<Long> productIds) {
        Map<Long, LocalDate> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return result;
        }
        for (Object[] row : batchRepository.earliestExpiry(productIds, BatchStatus.ACTIVE, today())) {
            result.put((Long) row[0], (LocalDate) row[1]);
        }
        return result;
    }

    public int discountPercent(Integer shelfLifeDays, LocalDate expiry) {
        if (expiry == null || shelfLifeDays == null || shelfLifeDays <= 0) {
            return 0;
        }
        long daysLeft = ChronoUnit.DAYS.between(today(), expiry);
        double ratio = (double) daysLeft / shelfLifeDays;
        if (ratio > 0.5) return 0;
        if (ratio > 0.25) return 20;
        return 50;
    }

    public double applyDiscount(Double basePrice, int percent) {
        if (basePrice == null) return 0;
        return Math.round(basePrice * (100 - percent)) / 100.0;
    }

    public double priceFor(Product product, LocalDate expiry) {
        return applyDiscount(product.getPrice(), discountPercent(product.getShelfLifeDays(), expiry));
    }

    public double currentPrice(Product product) {
        LocalDate expiry = earliestExpiry(java.util.List.of(product.getId())).get(product.getId());
        return priceFor(product, expiry);
    }
}
