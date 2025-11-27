// src/main/java/com/media/WebSocketConfig.java
package com.media;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtAuthChannelInterceptor jwtAuthChannelInterceptor;

    public WebSocketConfig(JwtAuthChannelInterceptor jwtAuthChannelInterceptor) {
        this.jwtAuthChannelInterceptor = jwtAuthChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WS endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // TODO: restrict in prod

        // SockJS fallback for older browsers or proxies
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client SEND goes to /app/**
        config.setApplicationDestinationPrefixes("/app");

        // Server can SEND to:
        // - /topic/** (broadcast)
        // - /queue/** (user-specific + with /user prefix)
        config.enableSimpleBroker("/topic", "/queue");

        // User-specific destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Intercept CONNECT frames to authenticate via JWT and set Principal
        registration.interceptors(jwtAuthChannelInterceptor);
    }
}
