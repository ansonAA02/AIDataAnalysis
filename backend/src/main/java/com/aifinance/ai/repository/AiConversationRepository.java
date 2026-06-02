package com.aifinance.ai.repository;

import java.util.List;

import com.aifinance.ai.domain.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for chat sessions; default ordering is most-recent first.
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    // Return all conversations newest-first by updated_at.
    List<AiConversation> findAllByOrderByUpdatedAtDesc();

    // Return only favorited conversations newest-first by updated_at.
    List<AiConversation> findAllByFavoritedTrueOrderByUpdatedAtDesc();
}
