package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.ChatRequest;
import com.phenikaa.tourService.dto.response.ChatResponse;
import com.phenikaa.tourService.service.interfaces.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tour/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIChatController {

    private final AIChatService aiChatService;

    /**
     * Xử lý tin nhắn chat từ user
     * 
     * @param request ChatRequest chứa message và sessionId
     * @return ChatResponse với câu trả lời từ AI
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> processMessage(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received chat message: {}", request.getMessage());

            ChatResponse response = aiChatService.processChatMessage(request);

            log.info("AI response type: {}", response.getResponseType());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);

            ChatResponse errorResponse = ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.")
                    .sessionId(request.getSessionId())
                    .responseType("error")
                    .build();

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Lấy thông tin trợ giúp chung
     * 
     * @return ChatResponse với thông tin trợ giúp
     */
    @GetMapping("/help")
    public ResponseEntity<ChatResponse> getGeneralHelp() {
        try {
            log.info("User requested general help");

            ChatResponse response = aiChatService.getGeneralHelp();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting general help: {}", e.getMessage(), e);

            ChatResponse errorResponse = ChatResponse.builder()
                    .message("Xin lỗi, tôi không thể cung cấp thông tin trợ giúp lúc này. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Tìm tour phù hợp dựa trên yêu cầu của user
     * 
     * @param userMessage    Tin nhắn từ user
     * @param specialization Chuyên môn/tập trung tìm kiếm
     * @return ChatResponse với gợi ý tour
     */
    @PostMapping("/suitable-tour")
    public ResponseEntity<ChatResponse> getSuitableTour(
            @RequestParam("message") String userMessage,
            @RequestParam(value = "specialization", defaultValue = "general") String specialization) {
        try {
            log.info("User requested suitable tour: {} with specialization: {}", userMessage, specialization);

            ChatResponse response = aiChatService.getSuitableTour(userMessage, specialization);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting suitable tour: {}", e.getMessage(), e);

            ChatResponse errorResponse = ChatResponse.builder()
                    .message("Xin lỗi, tôi không thể tìm tour phù hợp lúc này. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Phân tích intent của tin nhắn user
     * 
     * @param userMessage Tin nhắn từ user
     * @return String intent được phân tích
     */
    @GetMapping("/analyze-intent")
    public ResponseEntity<String> analyzeIntent(@RequestParam("message") String userMessage) {
        try {
            log.info("Analyzing intent for message: {}", userMessage);

            String intent = aiChatService.analyzeIntentSimple(userMessage);
            return ResponseEntity.ok(intent != null ? intent : "general_query");

        } catch (Exception e) {
            log.error("Error analyzing intent: {}", e.getMessage(), e);
            return ResponseEntity.ok("error");
        }
    }

    /**
     * Health check endpoint
     * 
     * @return String status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Chat Service is running");
    }
}

