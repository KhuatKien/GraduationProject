package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.ChatRequest;
import com.phenikaa.tourService.dto.response.ChatResponse;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.projection.TourSummaryProjection;
import com.phenikaa.tourService.repository.TourRepository;
import com.phenikaa.tourService.repository.ScheduleRepository;
import com.phenikaa.tourService.service.interfaces.AIChatService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {
    @Value("${ai.gemini.api-key:AIzaSyB5PT-SkQFhO-AWWxPpQFLb8tAtum0t-IM}")
    private String geminiApiKey;

    private final TourRepository tourRepository;
    private final ScheduleRepository scheduleRepository;

    private ChatModel getChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-1.5-flash")
                .temperature(0.7)
                .build();
    }

    @Override
    public ChatResponse getSuitableTour(String userMessage, String specialization) {
        return null;
    }

    @Override
    public ChatResponse getGeneralHelp() {
        String helpMessage = """
                **XIN CHÀO! TÔI LÀ AI TRỢ LÝ TƯ VẤN BOOKING TOUR**

                Tôi được thiết kế đặc biệt để hỗ trợ khách hàng trong quá trình đặt tour du lịch. Dưới đây là những gì tôi có thể giúp bạn:

                **1. TÌM KIẾM TOUR PHÙ HỢP**
                   Phân tích sở thích và ngân sách của bạn
                   Gợi ý tour theo danh mục (DOMESTIC, INTERNATIONAL, FAMILY, COUPLE, ADVENTURE)
                   Cung cấp thông tin chi tiết về tour (title, description, highlights)
                   Đề xuất tour theo điểm khởi hành và điểm đến
                   Đánh giá mức độ phổ biến (featured, isHot, hasPromotion)
                   Dự đoán trải nghiệm dựa trên duration và itinerary

                   **Ví dụ câu hỏi:**
                   - "Tôi muốn đi tour Đà Lạt 3 ngày 2 đêm"
                   - "Gợi ý tour quốc tế cho gia đình có trẻ em"
                   - "Tôi thích tour adventure, có tour nào phù hợp?"
                   - "Tour nào đang có khuyến mãi?"

                **2. KIỂM TRA LỊCH TRÌNH VÀ CHỖ TRỐNG**
                   Xem lịch khởi hành và trở về (departureDate, returnDate)
                   Kiểm tra số chỗ còn trống (availableSlots)
                   Thông tin chi tiết về giá người lớn và trẻ em (adultPrice, childPrice)
                   Cập nhật trạng thái tour (ACTIVE, INACTIVE, FULL, CANCELLED)
                   Kiểm tra trạng thái lịch trình (AVAILABLE, FULL, CANCELLED)

                   **Ví dụ câu hỏi:**
                   - "Tour Hạ Long ngày 15/12 còn chỗ không?"
                   - "Lịch trình tour Sapa cuối tuần có gì?"
                   - "Tour nào đang FULL hoặc CANCELLED?"
                   - "Giá tour này bao nhiêu cho người lớn và trẻ em?""";
        return ChatResponse.builder()
                .message(helpMessage)
                .sessionId("")
                .responseType("general_help")
                .build();
    }

    @Override
    public ChatResponse processChatMessage(ChatRequest request) {
        try {
            String userMessage = request.getMessage().toLowerCase();
            ChatModel model = getChatModel();

            // Kiểm tra các từ khóa đơn giản trước khi gọi AI
            String intent = analyzeIntentSimple(userMessage);
            if (intent == null) {
                // Nếu không xác định được bằng logic đơn giản, dùng AI
                intent = analyzeIntent(userMessage, model);
            }

            switch (intent) {
                case "general_help":
                    return getGeneralHelp();
                case "search_tour":
                    return searchTours(userMessage, model);
                case "check_schedule":
                    return checkTourSchedule(userMessage, model);
                case "tour_details":
                    return getTourDetails(userMessage, model);
                case "price_inquiry":
                    return getPriceInfo(userMessage, model);
                case "availability_check":
                    return checkAvailability(userMessage, model);
                case "category_tours":
                    return getToursByCategory(userMessage, model);
                case "featured_tours":
                    return getFeaturedTours(userMessage, model);
                case "hot_tours":
                    return getHotTours(userMessage, model);
                case "promotion_tours":
                    return getPromotionTours(userMessage, model);
                case "booking_help":
                    return getBookingHelp(userMessage, model);
                case "tour_comparison":
                    return compareTours(userMessage, model);
                case "destination_info":
                    return getDestinationInfo(userMessage, model);
                case "duration_tours":
                    return getToursByDuration(userMessage, model);
                case "price_range_tours":
                    return getToursByPriceRange(userMessage, model);
                default:
                    return handleGeneralQuery(request, model);
            }
        } catch (Exception e) {
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.")
                    .sessionId(request.getSessionId())
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public String analyzeIntentSimple(String userMessage) {
        // Tìm kiếm tour
        if (userMessage.contains("tìm tour") || userMessage.contains("search tour") ||
                userMessage.contains("gợi ý tour") || userMessage.contains("tour nào")) {
            return "search_tour";
        }

        // Kiểm tra lịch trình
        if (userMessage.contains("lịch trình") || userMessage.contains("schedule") ||
                userMessage.contains("ngày khởi hành") || userMessage.contains("departure")) {
            return "check_schedule";
        }

        // Chi tiết tour
        if (userMessage.contains("chi tiết") || userMessage.contains("details") ||
                userMessage.contains("mô tả") || userMessage.contains("highlights")) {
            return "tour_details";
        }

        // Hỏi giá
        if (userMessage.contains("giá") || userMessage.contains("price") ||
                userMessage.contains("bao nhiêu tiền") || userMessage.contains("cost")) {
            return "price_inquiry";
        }

        // Kiểm tra chỗ trống
        if (userMessage.contains("còn chỗ") || userMessage.contains("available") ||
                userMessage.contains("chỗ trống") || userMessage.contains("slot")) {
            return "availability_check";
        }

        // Tour theo danh mục
        if (userMessage.contains("domestic") || userMessage.contains("international") ||
                userMessage.contains("family") || userMessage.contains("couple") ||
                userMessage.contains("adventure") || userMessage.contains("nội địa") ||
                userMessage.contains("quốc tế") || userMessage.contains("gia đình")) {
            return "category_tours";
        }

        // Tour nổi bật
        if (userMessage.contains("nổi bật") || userMessage.contains("featured") ||
                userMessage.contains("đặc biệt")) {
            return "featured_tours";
        }

        // Tour hot
        if (userMessage.contains("hot") || userMessage.contains("trending") ||
                userMessage.contains("xu hướng")) {
            return "hot_tours";
        }

        // Tour khuyến mãi
        if (userMessage.contains("khuyến mãi") || userMessage.contains("promotion") ||
                userMessage.contains("giảm giá") || userMessage.contains("sale")) {
            return "promotion_tours";
        }

        // Hướng dẫn đặt tour
        if (userMessage.contains("đặt tour") || userMessage.contains("booking") ||
                userMessage.contains("reserve") || userMessage.contains("book")) {
            return "booking_help";
        }

        // So sánh tour
        if (userMessage.contains("so sánh") || userMessage.contains("compare") ||
                userMessage.contains("khác nhau")) {
            return "tour_comparison";
        }

        // Thông tin điểm đến
        if (userMessage.contains("điểm đến") || userMessage.contains("destination") ||
                userMessage.contains("địa điểm")) {
            return "destination_info";
        }

        // Tour theo thời gian
        if (userMessage.contains("ngày") || userMessage.contains("duration") ||
                userMessage.contains("thời gian") || userMessage.contains("bao lâu")) {
            return "duration_tours";
        }

        // Tour theo khoảng giá
        if (userMessage.contains("khoảng giá") || userMessage.contains("price range") ||
                userMessage.contains("từ") && userMessage.contains("đến")) {
            return "price_range_tours";
        }

        // Quản lý admin
        if (userMessage.contains("admin") || userMessage.contains("quản lý") ||
                userMessage.contains("manage") || userMessage.contains("update")) {
            return "admin_manage";
        }

        return null;
    }

    // Các method xử lý intent
    private ChatResponse searchTours(String userMessage, ChatModel model) {
        try {
            // Lấy 5 tour đầu tiên để gợi ý
            Pageable pageable = PageRequest.of(0, 5);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("🔍 **TÌM KIẾM TOUR PHÙ HỢP**\n\n");

            if (tours.hasContent()) {
                response.append("Dựa trên yêu cầu của bạn, tôi gợi ý các tour sau:\n\n");

                for (TourSummaryProjection tour : tours.getContent()) {
                    response.append("📍 **").append(tour.getTitle()).append("**\n");
                    response.append("   🎯 Điểm đến: ").append(tour.getDestination()).append("\n");
                    response.append("   🚀 Khởi hành: ").append(tour.getDeparture()).append("\n");
                    response.append("   💰 Giá người lớn: ").append(String.format("%,.0f", tour.getAdultPrice()))
                            .append(" VNĐ\n");
                    response.append("   ⏰ Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                    response.append("   🏷️ Danh mục: ").append(tour.getCategoryName()).append("\n");
                    if (tour.getFeatured())
                        response.append("   ⭐ Tour nổi bật\n");
                    if (tour.getIsHot())
                        response.append("   🔥 Tour hot\n");
                    if (tour.getHasPromotion())
                        response.append("   🎉 Có khuyến mãi\n");
                    response.append("\n");
                }

                response.append(
                        "💡 **Gợi ý:** Bạn có thể hỏi chi tiết về bất kỳ tour nào hoặc tìm tour theo danh mục cụ thể!");
            } else {
                response.append("Hiện tại chưa có tour nào phù hợp. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("search_tour")
                    .build();
        } catch (Exception e) {
            log.error("Error searching tours: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi tìm kiếm tour. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse checkTourSchedule(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang kiểm tra lịch trình tour...")
                .sessionId("")
                .responseType("check_schedule")
                .build();
    }

    private ChatResponse getTourDetails(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang lấy thông tin chi tiết tour...")
                .sessionId("")
                .responseType("tour_details")
                .build();
    }

    private ChatResponse getPriceInfo(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang kiểm tra giá tour...")
                .sessionId("")
                .responseType("price_inquiry")
                .build();
    }

    private ChatResponse checkAvailability(String userMessage, ChatModel model) {
        try {
            // Lấy tất cả lịch trình tour để kiểm tra chỗ trống
            List<TourSchedule> schedules = scheduleRepository.findAll();

            StringBuilder response = new StringBuilder("📅 **KIỂM TRA CHỖ TRỐNG**\n\n");

            if (!schedules.isEmpty()) {
                response.append("Tình trạng chỗ trống của các tour:\n\n");

                for (TourSchedule schedule : schedules) {
                    if (schedule.getTour() != null) {
                        response.append("🎯 **").append(schedule.getTour().getTitle()).append("**\n");
                        response.append("   📅 Ngày khởi hành: ").append(schedule.getDepartureDate()).append("\n");
                        response.append("   📅 Ngày trở về: ").append(schedule.getReturnDate()).append("\n");
                        response.append("   🎫 Chỗ trống: ").append(schedule.getAvailableSlots()).append(" chỗ\n");
                        response.append("   📊 Trạng thái: ").append(schedule.getStatus()).append("\n");
                        response.append("\n");
                    }
                }

                response.append("💡 **Gợi ý:** Bạn có thể đặt tour ngay khi còn chỗ trống!");
            } else {
                response.append("Hiện tại chưa có lịch trình tour nào. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("availability_check")
                    .build();
        } catch (Exception e) {
            log.error("Error checking availability: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi kiểm tra chỗ trống. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse getToursByCategory(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang tìm tour theo danh mục...")
                .sessionId("")
                .responseType("category_tours")
                .build();
    }

    private ChatResponse getFeaturedTours(String userMessage, ChatModel model) {
        try {
            // Tìm tour nổi bật (featured = true)
            Pageable pageable = PageRequest.of(0, 5);
            Page<TourSummaryProjection> featuredTours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("⭐ **TOUR NỔI BẬT**\n\n");

            if (featuredTours.hasContent()) {
                response.append("Đây là những tour nổi bật đang được ưa chuộng:\n\n");

                for (TourSummaryProjection tour : featuredTours.getContent()) {
                    if (tour.getFeatured()) {
                        response.append("🌟 **").append(tour.getTitle()).append("**\n");
                        response.append("   🎯 Điểm đến: ").append(tour.getDestination()).append("\n");
                        response.append("   🚀 Khởi hành: ").append(tour.getDeparture()).append("\n");
                        response.append("   💰 Giá: ").append(String.format("%,.0f", tour.getAdultPrice()))
                                .append(" VNĐ\n");
                        response.append("   ⏰ Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                        response.append("   🏷️ Danh mục: ").append(tour.getCategoryName()).append("\n");
                        response.append("\n");
                    }
                }

                response.append(
                        "💡 **Lưu ý:** Tour nổi bật thường có chất lượng cao và được nhiều khách hàng đánh giá tốt!");
            } else {
                response.append("Hiện tại chưa có tour nổi bật nào. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("featured_tours")
                    .build();
        } catch (Exception e) {
            log.error("Error getting featured tours: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi lấy tour nổi bật. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse getHotTours(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang tìm tour hot...")
                .sessionId("")
                .responseType("hot_tours")
                .build();
    }

    private ChatResponse getPromotionTours(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang tìm tour có khuyến mãi...")
                .sessionId("")
                .responseType("promotion_tours")
                .build();
    }

    private ChatResponse getBookingHelp(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang hướng dẫn đặt tour...")
                .sessionId("")
                .responseType("booking_help")
                .build();
    }

    private ChatResponse compareTours(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang so sánh các tour...")
                .sessionId("")
                .responseType("tour_comparison")
                .build();
    }

    private ChatResponse getDestinationInfo(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang lấy thông tin điểm đến...")
                .sessionId("")
                .responseType("destination_info")
                .build();
    }

    private ChatResponse getToursByDuration(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang tìm tour theo thời gian...")
                .sessionId("")
                .responseType("duration_tours")
                .build();
    }

    private ChatResponse getToursByPriceRange(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang tìm tour theo khoảng giá...")
                .sessionId("")
                .responseType("price_range_tours")
                .build();
    }

    private ChatResponse handleAdminRequest(String userMessage, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang xử lý yêu cầu quản lý...")
                .sessionId("")
                .responseType("admin_manage")
                .build();
    }

    private ChatResponse handleGeneralQuery(ChatRequest request, ChatModel model) {
        return ChatResponse.builder()
                .message("Tôi đang xử lý câu hỏi chung...")
                .sessionId(request.getSessionId())
                .responseType("general_query")
                .build();
    }

    @Override
    public String analyzeIntent(String userMessage, ChatModel model) {
        // TODO: Implement AI-based intent analysis
        return "general_query";
    }
}
