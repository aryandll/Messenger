// src/main/java/com/media/service/MessageService.java
package com.media.service;

import com.media.dto.*;

import java.util.List;

public interface MessageService {
    MessageDto send(String currentUsername, SendMessageRequest req);

    PageResponse<MessageDto> conversationPaged(String currentUsername, Long otherUserId, int page, int size);

    void delete(String currentUsername, Long messageId);

    /** Mark delivered (server-side when pushing to receiver). Returns updated MessageDto. */
    MessageDto markDelivered(Long messageId);

    /** Mark messages as read (receiver only), grouped by sender for notifications. */
    List<ReadReceiptDto> markRead(String currentUsername, List<Long> messageIds);
}
