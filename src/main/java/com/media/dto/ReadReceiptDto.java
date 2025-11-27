package com.media.dto;

import java.time.Instant;
import java.util.List;

public class ReadReceiptDto {
    private Long senderId;
    private String senderUsername;
    private Long readerId;
    private String readerUsername;
    private List<Long> messageIds;
    private Instant readAt;

    public ReadReceiptDto() {}

    // Convenience ctor
    public ReadReceiptDto(Long senderId, String senderUsername,
                          Long readerId, String readerUsername,
                          List<Long> messageIds, Instant readAt) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.readerId = readerId;
        this.readerUsername = readerUsername;
        this.messageIds = messageIds;
        this.readAt = readAt;
    }

    // Getters/Setters
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public String getReaderUsername() { return readerUsername; }
    public void setReaderUsername(String readerUsername) { this.readerUsername = readerUsername; }

    public List<Long> getMessageIds() { return messageIds; }
    public void setMessageIds(List<Long> messageIds) { this.messageIds = messageIds; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
