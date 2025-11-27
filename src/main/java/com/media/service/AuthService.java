// src/main/java/com/media/service/AuthService.java
package com.media.service;

import com.media.dto.AuthRequest;
import com.media.dto.AuthResponse;
import com.media.dto.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(AuthRequest request);
}
