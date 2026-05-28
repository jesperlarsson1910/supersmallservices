package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {}
