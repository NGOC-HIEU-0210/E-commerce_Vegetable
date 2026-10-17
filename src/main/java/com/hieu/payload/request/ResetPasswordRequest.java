package com.hieu.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String email;
    private String otp;
    private String newPassword;
}
