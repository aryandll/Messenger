// src/main/java/com/media/controller/WsMessageController.java
package com.media.controller;

import com.media.dto.MessageDto;
import com.media.dto.SendMessageRequest;
import com.media.repository.UserRepository;
import com.media.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WsMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepo;

    public WsMessageController(MessageService messageService,
                               SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepo) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepo = userRepo;
    }

    @MessageMapping("/messages.send")
    public void sendOverWs(Principal principal, SendMessageRequest req) {
        MessageDto created = messageService.send(principal.getName(), req);
        // Final variable for lambda capture
        final MessageDto delivered = messageService.markDelivered(created.getId());

        userRepo.findById(req.getReceiverId()).ifPresent(receiver ->
            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", delivered)
        );
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", delivered);
    }
}
