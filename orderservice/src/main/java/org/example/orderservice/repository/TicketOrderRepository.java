package org.example.orderservice.repository;

import org.example.orderservice.model.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {}
