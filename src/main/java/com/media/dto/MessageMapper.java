package com.media.dto;

import com.media.entity.Message;

public class MessageMapper {

    public static MessageDto toDto(Message m) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setSenderId(m.getSender().getId());
        dto.setReceiverId(m.getReceiver().getId());
        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setDeliveredAt(m.getDeliveredAt());
        dto.setReadAt(m.getReadAt());
        return dto;
    }
}
