package com.media.controller;

import com.media.dto.ReadReceiptDto;
import com.media.dto.ReadRequest;
import com.media.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class WsReceiptsController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public WsReceiptsController(MessageService messageService,
                                SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/messages.read")
    public void markRead(Principal principal, ReadRequest req) {
        List<ReadReceiptDto> receipts =
                messageService.markRead(principal.getName(), req.getMessageIds());
        for (ReadReceiptDto r : receipts) {
            messagingTemplate.convertAndSendToUser(r.getSenderUsername(), "/queue/read-receipts", r);
        }
    }
}
