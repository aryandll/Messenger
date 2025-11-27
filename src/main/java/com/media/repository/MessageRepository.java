// src/main/java/com/media/repository/MessageRepository.java
package com.media.repository;

import com.media.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Paged conversation in both directions
    Page<Message> findBySender_IdAndReceiver_IdOrSender_IdAndReceiver_Id(
            Long s1, Long r1, Long s2, Long r2, Pageable pageable
    );

    // For read receipts: only messages where this user is the receiver
    List<Message> findByIdInAndReceiver_Id(Collection<Long> ids, Long receiverId);
}
