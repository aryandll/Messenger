// src/main/java/com/media/controller/MessageController.java
package com.media.controller;

import com.media.dto.MessageDto;
import com.media.dto.PageResponse;
import com.media.dto.ReadReceiptDto;
import com.media.dto.ReadRequest;
import com.media.dto.SendMessageRequest;
import com.media.repository.UserRepository;
import com.media.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepo;

    public MessageController(MessageService messageService,
                             SimpMessagingTemplate messagingTemplate,
                             UserRepository userRepo) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<MessageDto> send(Authentication auth,
                                           @Valid @RequestBody SendMessageRequest req) {
        // Create
        MessageDto created = messageService.send(auth.getName(), req);
        // Mark delivered -> use a final variable for lambda capture
        final MessageDto delivered = messageService.markDelivered(created.getId());

        // Push to receiver (lambda captures 'delivered' which is final)
        userRepo.findById(req.getReceiverId()).ifPresent(receiver ->
            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", delivered)
        );

        // Echo to sender
        messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/messages", delivered);

        return ResponseEntity.ok().body(delivered);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<PageResponse<MessageDto>> conversationPaged(
            Authentication auth,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MessageDto> resp =
                messageService.conversationPaged(auth.getName(), otherUserId, page, size);
        return ResponseEntity.ok().body(resp);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long messageId) {
        messageService.delete(auth.getName(), messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read")
    public ResponseEntity<Void> read(Authentication auth, @Valid @RequestBody ReadRequest req) {
        List<ReadReceiptDto> receipts =
                messageService.markRead(auth.getName(), req.getMessageIds());
        for (ReadReceiptDto r : receipts) {
            messagingTemplate.convertAndSendToUser(r.getSenderUsername(), "/queue/read-receipts", r);
        }
        return ResponseEntity.noContent().build();
    }
}
