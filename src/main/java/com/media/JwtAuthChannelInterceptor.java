// src/main/java/com/media/JwtAuthChannelInterceptor.java
package com.media;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final com.media.serviceimpl.CustomUserDetailsService userDetailsService;

    public JwtAuthChannelInterceptor(JwtService jwtService,
                                     com.media.serviceimpl.CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public org.springframework.messaging.Message<?> preSend(
            org.springframework.messaging.Message<?> message,
            org.springframework.messaging.MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String username = jwtService.extractUsername(token);
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, user.getUsername())) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        accessor.setUser(authentication);
                    }
                } catch (Exception ignored) {
                    // If token is invalid, connection proceeds unauthenticated; user won't receive /user messages.
                }
            }
        }
        return message;
    }
}
