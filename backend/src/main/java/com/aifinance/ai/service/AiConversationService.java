package com.aifinance.ai.service;

import java.util.List;

import com.aifinance.ai.domain.AiConversation;
import com.aifinance.ai.domain.AiMessage;
import com.aifinance.ai.domain.AiMessageRole;
import com.aifinance.ai.dto.AiConversationDetailResponse;
import com.aifinance.ai.dto.AiConversationSummaryResponse;
import com.aifinance.ai.dto.AiMessageResponse;
import com.aifinance.ai.dto.AppendTurnRequest;
import com.aifinance.ai.repository.AiConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service layer for managing persistent AI chat history
// (list / detail / favorite / delete / append a Q&A turn).
@Service
public class AiConversationService {

    // Hard cap on title length to keep the history list readable.
    private static final int MAX_TITLE_LENGTH = 80;

    // Hard cap on the preview snippet shown in the conversation list.
    private static final int MAX_PREVIEW_LENGTH = 120;

    private final AiConversationRepository conversationRepository;

    public AiConversationService(AiConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    // List all conversations or only the favorited ones, newest-first.
    @Transactional(readOnly = true)
    public List<AiConversationSummaryResponse> list(boolean favoritesOnly) {
        List<AiConversation> rows = favoritesOnly
                ? conversationRepository.findAllByFavoritedTrueOrderByUpdatedAtDesc()
                : conversationRepository.findAllByOrderByUpdatedAtDesc();
        return rows.stream().map(this::toSummary).toList();
    }

    // Load a conversation with all its messages, throwing if it does not exist.
    @Transactional(readOnly = true)
    public AiConversationDetailResponse detail(Long id) {
        AiConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + id));
        return toDetail(conversation);
    }

    // Toggle the favorited flag and return the refreshed summary.
    @Transactional
    public AiConversationSummaryResponse toggleFavorite(Long id) {
        AiConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + id));
        conversation.setFavorited(!conversation.isFavorited());
        AiConversation saved = conversationRepository.save(conversation);
        return toSummary(saved);
    }

    // Permanently delete a conversation and its messages (cascade in DB).
    @Transactional
    public void delete(Long id) {
        if (!conversationRepository.existsById(id)) {
            return;
        }
        conversationRepository.deleteById(id);
    }

    // Append a user question + assistant answer pair, creating a session if needed.
    @Transactional
    public AiConversationDetailResponse appendTurn(AppendTurnRequest request) {
        AiConversation conversation;
        if (request.conversationId() == null) {
            // First turn: derive a title from the question and create a new session.
            conversation = new AiConversation(buildTitle(request.question()), request.periodId());
        } else {
            conversation = conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Conversation not found: " + request.conversationId()));
            if (request.periodId() != null) {
                conversation.setPeriodId(request.periodId());
            }
        }

        if (request.question() != null && !request.question().isBlank()) {
            conversation.addMessage(new AiMessage(AiMessageRole.USER, request.question().trim()));
        }
        if (request.answer() != null && !request.answer().isBlank()) {
            conversation.addMessage(new AiMessage(AiMessageRole.ASSISTANT, request.answer().trim()));
        }

        AiConversation saved = conversationRepository.save(conversation);
        return toDetail(saved);
    }

    // Build a list-friendly summary including a short preview of the latest reply.
    private AiConversationSummaryResponse toSummary(AiConversation conversation) {
        String preview = conversation.getMessages().stream()
                .reduce((first, second) -> second)
                .map(AiMessage::getContent)
                .orElse("")
                .replaceAll("\\s+", " ")
                .trim();
        if (preview.length() > MAX_PREVIEW_LENGTH) {
            preview = preview.substring(0, MAX_PREVIEW_LENGTH) + "…";
        }
        return new AiConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getPeriodId(),
                conversation.isFavorited(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getMessages().size(),
                preview);
    }

    // Build the full detail payload including every message in chronological order.
    private AiConversationDetailResponse toDetail(AiConversation conversation) {
        List<AiMessageResponse> messages = conversation.getMessages().stream()
                .map(message -> new AiMessageResponse(
                        message.getId(),
                        message.getRole().name().toLowerCase(),
                        message.getContent(),
                        message.getCreatedAt()))
                .toList();
        return new AiConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getPeriodId(),
                conversation.isFavorited(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages);
    }

    // Truncate the user's first question into a clean session title.
    private String buildTitle(String question) {
        if (question == null || question.isBlank()) {
            return "新对话";
        }
        String normalized = question.replaceAll("\\s+", " ").trim();
        if (normalized.length() > MAX_TITLE_LENGTH) {
            normalized = normalized.substring(0, MAX_TITLE_LENGTH) + "…";
        }
        return normalized;
    }
}
