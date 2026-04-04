package com.uninode.smartcampus.modules.tickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uninode.smartcampus.modules.tickets.entity.TicketAttachmentEntity;

import java.util.List;

@Repository
public interface TicketAttachmentEntityRepository extends JpaRepository<TicketAttachmentEntity, Long> {
    List<TicketAttachmentEntity> findByTicketTicketId(Long ticketId);
    long countByTicketTicketId(Long ticketId);
}
