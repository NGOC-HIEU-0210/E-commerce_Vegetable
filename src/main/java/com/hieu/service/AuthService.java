package com.hieu.service;

import com.hieu.payload.request.ForgotPasswordRequest;
import com.hieu.payload.request.LoginRequest;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.request.ResetPasswordRequest;
import com.hieu.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse signup(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
