package com.media.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReadRequest {
    @NotEmpty
    private List<Long> messageIds;

    public ReadRequest() {}

    public List<Long> getMessageIds() { return messageIds; }
    public void setMessageIds(List<Long> messageIds) { this.messageIds = messageIds; }
}
