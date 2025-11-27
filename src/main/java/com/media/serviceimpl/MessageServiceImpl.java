package com.media.serviceimpl;

import com.media.dto.*;
import com.media.entity.Message;
import com.media.entity.User;
import com.media.repository.MessageRepository;
import com.media.repository.UserRepository;
import com.media.service.MessageService;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    public MessageServiceImpl(MessageRepository messageRepo, UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    @Override
    public MessageDto send(String currentUsername, SendMessageRequest req) {
        User sender = userRepo.findByUsername(currentUsername).orElseThrow();
        User receiver = userRepo.findById(req.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }

        Message m = new Message();
        m.setSender(sender);
        m.setReceiver(receiver);
        m.setContent(req.getContent());
        m.setDeletedBySender(false);
        m.setDeletedByReceiver(false);

        messageRepo.save(m);
        return MessageMapper.toDto(m);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> conversationPaged(String currentUsername, Long otherUserId, int page, int size) {
        User me = userRepo.findByUsername(currentUsername).orElseThrow();
        User other = userRepo.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Other user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Message> p = messageRepo.findBySender_IdAndReceiver_IdOrSender_IdAndReceiver_Id(
                me.getId(), other.getId(), other.getId(), me.getId(), pageable
        );

        List<MessageDto> content = p.getContent().stream()
                .filter(m -> {
                    boolean iAmSender = m.getSender().getId().equals(me.getId());
                    if (iAmSender && m.isDeletedBySender()) return false;
                    if (!iAmSender && m.isDeletedByReceiver()) return false;
                    return true;
                })
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<MessageDto> resp = new PageResponse<>();
        resp.setContent(content);
        resp.setPage(p.getNumber());
        resp.setSize(p.getSize());
        resp.setTotalElements(p.getTotalElements());
        resp.setTotalPages(p.getTotalPages());
        resp.setLast(p.isLast());
        return resp;
    }

    @Override
    public void delete(String currentUsername, Long messageId) {
        User me = userRepo.findByUsername(currentUsername).orElseThrow();
        Message m = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        boolean iAmSender = m.getSender().getId().equals(me.getId());
        boolean iAmReceiver = m.getReceiver().getId().equals(me.getId());

        if (!iAmSender && !iAmReceiver) {
            throw new AccessDeniedException("You are not a participant of this message");
        }

        if (iAmSender) m.setDeletedBySender(true);
        if (iAmReceiver) m.setDeletedByReceiver(true);

        if (m.isDeletedBySender() && m.isDeletedByReceiver()) {
            messageRepo.delete(m);
        } else {
            messageRepo.save(m);
        }
    }

    @Override
    public MessageDto markDelivered(Long messageId) {
        Message m = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (m.getDeliveredAt() == null) {
            m.setDeliveredAt(Instant.now());
            messageRepo.save(m);
        }
        return MessageMapper.toDto(m);
    }

    @Override
    public List<ReadReceiptDto> markRead(String currentUsername, List<Long> messageIds) {
        User me = userRepo.findByUsername(currentUsername).orElseThrow();
        if (messageIds == null || messageIds.isEmpty()) return Collections.emptyList();

        List<Message> messages = messageRepo.findByIdInAndReceiver_Id(messageIds, me.getId());
        if (messages.isEmpty()) return Collections.emptyList();

        Instant now = Instant.now();

        for (Message m : messages) {
            if (m.getReadAt() == null) {
                m.setReadAt(now);
            }
        }
        messageRepo.saveAll(messages);

        Map<User, List<Message>> bySender = messages.stream()
                .collect(Collectors.groupingBy(Message::getSender));

        List<ReadReceiptDto> receipts = new ArrayList<>();
        for (Map.Entry<User, List<Message>> e : bySender.entrySet()) {
            User sender = e.getKey();
            List<Long> ids = e.getValue().stream().map(Message::getId).collect(Collectors.toList());
            receipts.add(new ReadReceiptDto(
                    sender.getId(),
                    sender.getUsername(),
                    me.getId(),
                    me.getUsername(),
                    ids,
                    now
            ));
        }
        return receipts;
    }
}
