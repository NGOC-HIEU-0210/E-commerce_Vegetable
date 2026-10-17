package com.hieu.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {

    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String note;
    private String couponCode;
    private Long userId;
    private Long statusId;
    private List<CheckoutItem> items;

    @Getter
    @Setter
    public static class CheckoutItem {
        private Long productId;
        private Integer quantity;
    }
}
