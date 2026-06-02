import http from './http';
import type {
  AiAnswer,
  AiAskRequest,
  AiConversationDetail,
  AiConversationSummary,
  AppendTurnPayload,
  ApiResponse,
} from '../types/finance';

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '');

export async function getAiSummary(periodId: number): Promise<AiAnswer> {
  const response = await http.get<ApiResponse<AiAnswer>>('/api/ai/summary', {
    params: { periodId },
  });
  return response.data.data;
}

export async function askAi(request: AiAskRequest): Promise<AiAnswer> {
  const response = await http.post<ApiResponse<AiAnswer>>('/api/ai/ask', request);
  return response.data.data;
}

export async function getAiSuggestions(periodId: number): Promise<string[]> {
  const response = await http.get<ApiResponse<string[]>>('/api/ai/suggestions', {
    params: { periodId },
  });
  return response.data.data;
}

export async function streamAiAnswer(
  request: AiAskRequest,
  onChunk: (chunk: string) => void,
): Promise<void> {
  const response = await fetch(`${apiBaseUrl}/api/ai/ask/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`AI stream request failed with status ${response.status}`);
  }

  const reader = response.body?.getReader();
  if (!reader) {
    throw new Error('AI stream response body is not readable');
  }

  const decoder = new TextDecoder('utf-8');
  let buffer = '';
  let eventData = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split(/\r?\n/);
    buffer = lines.pop() ?? '';

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const text = line.startsWith('data: ') ? line.slice(6) : line.slice(5);
        eventData = eventData ? `${eventData}\n${text}` : text;
      } else if (line === '' && eventData) {
        onChunk(eventData);
        eventData = '';
      }
    }
  }

  if (eventData) {
    onChunk(eventData);
  }
}

// List stored AI conversations; pass favoritesOnly=true for favorites view.
export async function listConversations(favoritesOnly = false): Promise<AiConversationSummary[]> {
  const response = await http.get<ApiResponse<AiConversationSummary[]>>('/api/ai/conversations', {
    params: { favoritesOnly },
  });
  return response.data.data;
}

// Load a single conversation with its full message history.
export async function getConversationDetail(id: number): Promise<AiConversationDetail> {
  const response = await http.get<ApiResponse<AiConversationDetail>>(`/api/ai/conversations/${id}`);
  return response.data.data;
}

// Persist a fresh user/assistant turn; pass conversationId=null to start a new session.
export async function appendConversationTurn(
  payload: AppendTurnPayload,
): Promise<AiConversationDetail> {
  const response = await http.post<ApiResponse<AiConversationDetail>>(
    '/api/ai/conversations/turns',
    payload,
  );
  return response.data.data;
}

// Toggle the favorited flag on a conversation.
export async function toggleConversationFavorite(id: number): Promise<AiConversationSummary> {
  const response = await http.post<ApiResponse<AiConversationSummary>>(
    `/api/ai/conversations/${id}/favorite`,
  );
  return response.data.data;
}

// Permanently delete a conversation (and its messages).
export async function deleteConversation(id: number): Promise<void> {
  await http.delete<ApiResponse<void>>(`/api/ai/conversations/${id}`);
}
