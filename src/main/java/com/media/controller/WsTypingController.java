package com.media.controller;

import com.media.dto.TypingDto;
import com.media.entity.User;
import com.media.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WsTypingController {

    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public WsTypingController(UserRepository userRepo,
                              SimpMessagingTemplate messagingTemplate) {
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/typing")
    public void typing(Principal principal, TypingDto dto) {
        User from = userRepo.findByUsername(principal.getName()).orElseThrow();
        User to = userRepo.findById(dto.getToUserId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        TypingDto out = new TypingDto();
        out.setFromUserId(from.getId());
        out.setFromUsername(from.getUsername());
        out.setToUserId(to.getId());
        out.setTyping(dto.isTyping());

        messagingTemplate.convertAndSendToUser(to.getUsername(), "/queue/typing", out);
    }
}
