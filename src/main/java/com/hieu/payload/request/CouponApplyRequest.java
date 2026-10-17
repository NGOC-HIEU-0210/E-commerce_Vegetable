package com.hieu.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponApplyRequest {

    private String code;
    private Double orderTotal;
}
