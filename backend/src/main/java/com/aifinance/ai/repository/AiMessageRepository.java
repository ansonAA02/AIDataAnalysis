package com.aifinance.ai.repository;

import com.aifinance.ai.domain.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for individual chat messages.
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
}
