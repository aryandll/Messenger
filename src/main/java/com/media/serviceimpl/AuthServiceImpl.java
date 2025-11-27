package com.media.serviceimpl;

import com.media.JwtService;
import com.media.dto.AuthRequest;
import com.media.dto.AuthResponse;
import com.media.dto.SignupRequest;
import com.media.entity.User;
import com.media.repository.UserRepository;
import com.media.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepo,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authManager,
                           JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User u = new User();
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setRole("USER");

        userRepo.save(u);

        String token = jwtService.generateToken(u.getUsername(), Map.of("userId", u.getId()));
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()
            ));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }
        User u = userRepo.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(u.getUsername(), Map.of("userId", u.getId()));
        return new AuthResponse(token);
    }
}
