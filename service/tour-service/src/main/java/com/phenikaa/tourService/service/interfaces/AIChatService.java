package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.request.ChatRequest;
import com.phenikaa.tourService.dto.response.ChatResponse;

public interface AIChatService {
    ChatResponse getSuitableTour(String userMessage, String specialization);

    ChatResponse getGeneralHelp();

    ChatResponse processChatMessage(ChatRequest request);

    String analyzeIntentSimple(String userMessage);

    String analyzeIntent(String userMessage, dev.langchain4j.model.chat.ChatModel model);
}
