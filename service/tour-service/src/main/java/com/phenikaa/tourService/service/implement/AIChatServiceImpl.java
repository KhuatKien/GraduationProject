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
                .modelName("gemini-2.0-flash")
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
                   Gợi ý tour theo danh mục (Du lịch biển, Du lịch núi, Du lịch văn hóa, Du lịch thành phố, Du lịch sinh thái, Du lịch ẩm thực, Du lịch tâm linh, Du lịch gia đình, Du lịch cao cấp, Du lịch tiết kiệm)
                   Cung cấp thông tin chi tiết về tour (title, description, highlights)
                   Đề xuất tour theo điểm khởi hành và điểm đến
                   Đánh giá mức độ phổ biến (featured, isHot, hasPromotion)
                   Dự đoán trải nghiệm dựa trên duration và itinerary

                   **Ví dụ câu hỏi:**
                   - "Tôi muốn đi tour 3 ngày 2 đêm"
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
            // Lấy tất cả tour để AI có thể lọc thông minh
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("🔍 **TÌM KIẾM TOUR PHÙ HỢP**\n\n");

            if (tours.hasContent()) {
                // Tạo dữ liệu tour chi tiết cho AI
                StringBuilder tourData = new StringBuilder();
                for (TourSummaryProjection tour : tours.getContent()) {
                    tourData.append("TOUR: ").append(tour.getTitle()).append("\n");
                    tourData.append("  - Điểm đến: ").append(tour.getDestination()).append("\n");
                    tourData.append("  - Khởi hành: ").append(tour.getDeparture()).append("\n");
                    tourData.append("  - Danh mục: ").append(tour.getCategoryName()).append("\n");
                    tourData.append("  - Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                    tourData.append("  - Giá người lớn: ").append(String.format("%,.0f", tour.getAdultPrice()))
                            .append(" VNĐ\n");
                    tourData.append("  - Giá trẻ em: ").append(String.format("%,.0f", tour.getChildPrice()))
                            .append(" VNĐ\n");
                    tourData.append("  - Đánh giá: 4.5/5 (10 đánh giá)\n");
                    tourData.append("  - Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append("\n");
                    tourData.append("  - Hot: ").append(tour.getIsHot() ? "Có" : "Không").append("\n");
                    tourData.append("  - Khuyến mãi: ").append(tour.getHasPromotion() ? "Có" : "Không").append("\n");
                    tourData.append("  - Trạng thái: ").append(tour.getStatus()).append("\n\n");
                }

                String aiPrompt = String.format("""
                        Bạn là chuyên gia tư vấn tour du lịch. Dựa trên yêu cầu của khách hàng: "%s"

                        Và danh sách tour có sẵn:
                        %s

                        Hãy phân tích và chọn 4-6 tour phù hợp nhất, sắp xếp theo độ phù hợp giảm dần.
                        Trả lời ngắn gọn và thân thiện:

                        🎯 **TÌM KIẾM TOUR PHÙ HỢP**

                        Tôi đã tìm thấy %d tour phù hợp với yêu cầu của bạn. Dưới đây là các tour được đề xuất:

                        💡 **Gợi ý chọn tour:**
                        - Xem xét ngân sách và thời gian phù hợp
                        - Kiểm tra lịch trình và chỗ trống
                        - Đọc đánh giá từ khách hàng trước
                        - Liên hệ để được tư vấn chi tiết

                        📞 **Hỗ trợ đặt tour:** 1900-xxxx
                        """, userMessage, tourData.toString(), Math.min(tours.getContent().size(), 6));

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
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
        try {
            // Lấy tất cả tour để phân tích giá
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("💰 **THÔNG TIN GIÁ TOUR**\n\n");

            if (tours.hasContent()) {
                // Tạo dữ liệu giá cho AI phân tích
                StringBuilder priceData = new StringBuilder();
                for (TourSummaryProjection tour : tours.getContent()) {
                    priceData.append("TOUR: ").append(tour.getTitle()).append("\n");
                    priceData.append("  - Điểm đến: ").append(tour.getDestination()).append("\n");
                    priceData.append("  - Danh mục: ").append(tour.getCategoryName()).append("\n");
                    priceData.append("  - Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                    priceData.append("  - Giá người lớn: ").append(String.format("%,.0f", tour.getAdultPrice()))
                            .append(" VNĐ\n");
                    priceData.append("  - Giá trẻ em: ").append(String.format("%,.0f", tour.getChildPrice()))
                            .append(" VNĐ\n");
                    priceData.append("  - Đánh giá: 4.5/5\n");
                    priceData.append("  - Khuyến mãi: ").append(tour.getHasPromotion() ? "Có" : "Không").append("\n");
                    priceData.append("  - Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append("\n");
                    priceData.append("  - Hot: ").append(tour.getIsHot() ? "Có" : "Không").append("\n\n");
                }

                String aiPrompt = String.format("""
                        Bạn là chuyên gia phân tích giá tour du lịch. Dựa trên yêu cầu: "%s"

                        Và dữ liệu giá tour hiện tại:
                        %s

                        Hãy phân tích và trả lời ngắn gọn:

                        📊 **PHÂN TÍCH GIÁ TOUR**

                        Tôi đã phân tích %d tour hiện có. Dưới đây là thông tin giá chi tiết:

                        💰 **Khoảng giá phổ biến:**
                        - Dưới 3 triệu: Tour ngắn ngày, phù hợp ngân sách hạn chế
                        - 3-5 triệu: Tour tiêu chuẩn, cân bằng giá/chất lượng
                        - 5-8 triệu: Tour cao cấp, trải nghiệm đầy đủ
                        - Trên 8 triệu: Tour luxury, dịch vụ 5 sao

                        🎯 **Gợi ý chọn tour theo ngân sách:**
                        - Ngân sách thấp: Chọn tour 2-3 ngày, khởi hành gần
                        - Ngân sách trung bình: Tour 3-4 ngày, có khuyến mãi
                        - Ngân sách cao: Tour 4-5 ngày, resort cao cấp

                        💡 **Mẹo tiết kiệm:**
                        - Đặt tour sớm để có giá tốt
                        - Chọn tour khuyến mãi
                        - Đi theo nhóm để giảm giá
                        - Tránh mùa cao điểm

                        📞 **Tư vấn giá chi tiết:** 1900-xxxx
                        """, userMessage, priceData.toString(), tours.getContent().size());

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
            } else {
                response.append("Hiện tại chưa có thông tin giá tour. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("price_inquiry")
                    .build();
        } catch (Exception e) {
            log.error("Error getting price info: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi lấy thông tin giá. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse checkAvailability(String userMessage, ChatModel model) {
        try {
            // Lấy tất cả lịch trình tour để kiểm tra chỗ trống
            List<TourSchedule> schedules = scheduleRepository.findAll();

            StringBuilder response = new StringBuilder("📅 **KIỂM TRA CHỖ TRỐNG**\n\n");

            if (!schedules.isEmpty()) {
                // Tạo dữ liệu lịch trình cho AI phân tích
                StringBuilder scheduleData = new StringBuilder();
                for (TourSchedule schedule : schedules) {
                    if (schedule.getTour() != null) {
                        scheduleData.append("TOUR: ").append(schedule.getTour().getTitle()).append("\n");
                        scheduleData.append("  - Điểm đến: ").append(schedule.getTour().getDestination()).append("\n");
                        scheduleData.append("  - Khởi hành: ").append(schedule.getDepartureDate()).append("\n");
                        scheduleData.append("  - Trở về: ").append(schedule.getReturnDate()).append("\n");
                        scheduleData.append("  - Chỗ trống: ").append(schedule.getAvailableSlots()).append(" chỗ\n");
                        scheduleData.append("  - Trạng thái: ").append(schedule.getStatus()).append("\n");
                        scheduleData.append("  - Giá: ")
                                .append(String.format("%,.0f", schedule.getTour().getAdultPrice())).append(" VNĐ\n\n");
                    }
                }

                String aiPrompt = String.format("""
                        Bạn là chuyên gia quản lý lịch trình tour. Dựa trên yêu cầu: "%s"

                        Và dữ liệu lịch trình tour hiện tại:
                        %s

                        Hãy phân tích và trả lời theo format:

                        📊 **TÌNH TRẠNG CHỖ TRỐNG**

                        🟢 **TOUR CÒN CHỖ (AVAILABLE):**
                        [Liệt kê các tour còn chỗ với thông tin chi tiết]

                        🔴 **TOUR HẾT CHỖ (FULL):**
                        [Liệt kê các tour đã hết chỗ]

                        ⚠️ **TOUR HẾT HẠN (EXPIRED):**
                        [Liệt kê các tour đã hết hạn]

                        📅 **THEO THỜI GIAN:**
                        - Tuần này: [số lượng] tour
                        - Tuần sau: [số lượng] tour
                        - Tháng sau: [số lượng] tour

                        🎯 **GỢI Ý ĐẶT TOUR:**
                        - Tour nên đặt ngay: [tên tour] - [lý do]
                        - Tour có nhiều chỗ: [tên tour] - [số chỗ]
                        - Tour sắp hết chỗ: [tên tour] - [số chỗ còn lại]

                        💡 **LỜI KHUYÊN:**
                        - Nên đặt tour trước bao lâu: [khuyến nghị]
                        - Cách chọn thời gian phù hợp: [gợi ý]
                        - Lưu ý đặc biệt: [cảnh báo về thời gian, mùa cao điểm, etc.]
                        """, userMessage, scheduleData.toString());

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
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
        try {
            // Lấy tất cả tour để phân loại theo danh mục
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("🏷️ **TOUR THEO DANH MỤC**\n\n");

            if (tours.hasContent()) {
                // Tạo dữ liệu tour theo danh mục cho AI phân tích
                StringBuilder categoryData = new StringBuilder();
                for (TourSummaryProjection tour : tours.getContent()) {
                    String category = tour.getCategoryName() != null ? tour.getCategoryName() : "Chưa phân loại";
                    categoryData.append("TOUR: ").append(tour.getTitle()).append("\n");
                    categoryData.append("  - Danh mục: ").append(category).append("\n");
                    categoryData.append("  - Điểm đến: ").append(tour.getDestination()).append("\n");
                    categoryData.append("  - Khởi hành: ").append(tour.getDeparture()).append("\n");
                    categoryData.append("  - Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                    categoryData.append("  - Giá: ").append(String.format("%,.0f", tour.getAdultPrice()))
                            .append(" VNĐ\n");
                    categoryData.append("  - Đánh giá: 4.5/5\n");
                    categoryData.append("  - Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append("\n");
                    categoryData.append("  - Hot: ").append(tour.getIsHot() ? "Có" : "Không").append("\n");
                    categoryData.append("  - Khuyến mãi: ").append(tour.getHasPromotion() ? "Có" : "Không")
                            .append("\n\n");
                }

                String aiPrompt = String.format("""
                        Bạn là chuyên gia phân loại tour du lịch. Dựa trên yêu cầu: "%s"

                        Và dữ liệu tour hiện tại:
                        %s

                        Hãy phân tích và trả lời ngắn gọn:

                        📊 **PHÂN LOẠI TOUR THEO DANH MỤC**

                        Tôi đã tìm thấy %d tour được phân loại theo danh mục. Dưới đây là các loại tour phổ biến:

                        🏖️ **Du lịch biển:**
                        - Điểm đến: Nha Trang, Phú Quốc, Hạ Long, Đà Nẵng
                        - Đặc điểm: Bãi biển đẹp, hoạt động dưới nước, resort cao cấp
                        - Phù hợp: Gia đình, cặp đôi, nhóm bạn

                        🏔️ **Du lịch núi:**
                        - Điểm đến: Sapa, Đà Lạt, Mộc Châu, Fansipan
                        - Đặc điểm: Khí hậu mát mẻ, cảnh đẹp thiên nhiên, trekking
                        - Phù hợp: Người thích khám phá, adventure

                        🏛️ **Du lịch văn hóa:**
                        - Điểm đến: Huế, Hội An, Hà Nội, TP.HCM
                        - Đặc điểm: Lịch sử, di sản, ẩm thực, kiến trúc cổ
                        - Phù hợp: Người yêu thích lịch sử, văn hóa

                        🌿 **Du lịch sinh thái:**
                        - Điểm đến: Cát Tiên, U Minh, Tràm Chim, Côn Đảo
                        - Đặc điểm: Thiên nhiên hoang dã, bảo tồn, khám phá
                        - Phù hợp: Người yêu thiên nhiên, nghiên cứu

                        💡 **Gợi ý chọn danh mục:**
                        - Nếu thích biển: Chọn tour biển 3-4 ngày
                        - Nếu thích núi: Chọn tour núi 2-3 ngày
                        - Nếu thích văn hóa: Chọn tour văn hóa 2-4 ngày
                        - Nếu thích sinh thái: Chọn tour sinh thái 2-3 ngày

                        📞 **Tư vấn tour theo danh mục:** 1900-xxxx
                        """, userMessage, categoryData.toString(), tours.getContent().size());

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
            } else {
                response.append("Hiện tại chưa có tour nào. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("category_tours")
                    .build();
        } catch (Exception e) {
            log.error("Error getting tours by category: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi lấy tour theo danh mục. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse getFeaturedTours(String userMessage, ChatModel model) {
        try {
            // Lấy tất cả tour để tìm tour nổi bật
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("⭐ **TOUR NỔI BẬT**\n\n");

            if (tours.hasContent()) {

                String aiPrompt = String.format(
                        """
                                Bạn là chuyên gia đánh giá tour du lịch. Dựa trên yêu cầu: "%s"

                                Hãy phân tích và trả lời ngắn gọn:

                                🌟 **TOUR NỔI BẬT HÀNG ĐẦU**

                                Tôi đã tìm thấy tour nổi bật phù hợp với yêu cầu của bạn. Đây là những tour được đánh giá cao nhất:

                                ⭐ **Đặc điểm tour nổi bật:**
                                - Chất lượng dịch vụ cao cấp
                                - Được nhiều khách hàng đánh giá tốt
                                - Lộ trình được thiết kế tối ưu
                                - Hỗ trợ khách hàng 24/7

                                🎯 **Gợi ý lựa chọn:**
                                - Ngân sách dưới 3 triệu: Tour ngắn ngày, chất lượng tốt
                                - Ngân sách 3-5 triệu: Tour cân bằng giá/chất lượng
                                - Ngân sách cao: Tour luxury, trải nghiệm đầy đủ

                                💡 **Tại sao chọn tour nổi bật:**
                                - Đảm bảo chất lượng dịch vụ
                                - Lộ trình đã được kiểm chứng
                                - Hỗ trợ khách hàng chuyên nghiệp
                                - Giá trị tốt nhất cho tiền bạc

                                📞 **Tư vấn tour nổi bật:** 1900-xxxx
                                """,
                        userMessage);

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
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
        try {
            // Lấy tất cả tour để tìm tour hot
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("🔥 **TOUR HOT - XU HƯỚNG**\n\n");

            if (tours.hasContent()) {
                // Tạo dữ liệu tour hot cho AI phân tích
                StringBuilder hotData = new StringBuilder();
                for (TourSummaryProjection tour : tours.getContent()) {
                    if (tour.getIsHot()) {
                        hotData.append("TOUR HOT: ").append(tour.getTitle()).append("\n");
                        hotData.append("  - Điểm đến: ").append(tour.getDestination()).append("\n");
                        hotData.append("  - Khởi hành: ").append(tour.getDeparture()).append("\n");
                        hotData.append("  - Danh mục: ").append(tour.getCategoryName()).append("\n");
                        hotData.append("  - Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                        hotData.append("  - Giá người lớn: ").append(String.format("%,.0f", tour.getAdultPrice()))
                                .append(" VNĐ\n");
                        hotData.append("  - Giá trẻ em: ").append(String.format("%,.0f", tour.getChildPrice()))
                                .append(" VNĐ\n");
                        hotData.append("  - Đánh giá: 4.5/5 (10 đánh giá)\n");
                        hotData.append("  - Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append("\n");
                        hotData.append("  - Khuyến mãi: ").append(tour.getHasPromotion() ? "Có" : "Không")
                                .append("\n\n");
                    }
                }

                String aiPrompt = String.format("""
                        Bạn là chuyên gia phân tích xu hướng du lịch. Dựa trên yêu cầu: "%s"

                        Và danh sách tour hot hiện có:
                        %s

                        Hãy phân tích và trả lời ngắn gọn:

                        🔥 **TOUR HOT - XU HƯỚNG HIỆN TẠI**

                        Tôi đã tìm thấy %d tour hot đang trending. Đây là những tour được yêu thích nhất hiện tại:

                        ⚡ **Lý do nên chọn tour hot:**
                        - Được nhiều người tin tưởng và lựa chọn
                        - Chất lượng dịch vụ đã được kiểm chứng
                        - Có nhiều đánh giá tích cực
                        - Thường có ưu đãi đặc biệt

                        🎯 **Xu hướng du lịch hiện tại:**
                        - Điểm đến hot: Biển, núi, văn hóa
                        - Thời gian phổ biến: 2-4 ngày
                        - Khoảng giá: 3-6 triệu VNĐ
                        - Đối tượng: Gia đình, cặp đôi, nhóm bạn

                        💡 **Gợi ý chọn tour hot:**
                        - Nếu muốn trải nghiệm mới: Chọn tour mới nhất
                        - Nếu muốn an toàn: Chọn tour có nhiều đánh giá
                        - Nếu muốn tiết kiệm: Chọn tour có khuyến mãi
                        - Nếu muốn độc đáo: Chọn tour ít người biết

                        📞 **Tư vấn tour hot:** 1900-xxxx

                        🚨 **CẢNH BÁO:**
                        - Tour hot thường được đặt nhanh
                        - Nên đặt trước ít nhất 1-2 tuần
                        - Kiểm tra chỗ trống thường xuyên

                        💡 **GỢI Ý ĐẶT TOUR HOT:**
                        - Nên đặt sớm để có giá tốt
                        - Theo dõi các chương trình khuyến mãi
                        - Chọn thời gian phù hợp với lịch trình
                        """, userMessage, hotData.toString());

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
            } else {
                response.append("Hiện tại chưa có tour hot nào. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("hot_tours")
                    .build();
        } catch (Exception e) {
            log.error("Error getting hot tours: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi lấy tour hot. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    private ChatResponse getPromotionTours(String userMessage, ChatModel model) {
        try {
            // Lấy tất cả tour để tìm tour khuyến mãi
            Pageable pageable = PageRequest.of(0, 20);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder response = new StringBuilder("🎉 **TOUR KHUYẾN MÃI**\n\n");

            if (tours.hasContent()) {
                // Tạo dữ liệu tour khuyến mãi cho AI phân tích
                StringBuilder promotionData = new StringBuilder();
                for (TourSummaryProjection tour : tours.getContent()) {
                    if (tour.getHasPromotion()) {
                        promotionData.append("TOUR KHUYẾN MÃI: ").append(tour.getTitle()).append("\n");
                        promotionData.append("  - Điểm đến: ").append(tour.getDestination()).append("\n");
                        promotionData.append("  - Khởi hành: ").append(tour.getDeparture()).append("\n");
                        promotionData.append("  - Danh mục: ").append(tour.getCategoryName()).append("\n");
                        promotionData.append("  - Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                        promotionData.append("  - Giá gốc: ").append(String.format("%,.0f", tour.getAdultPrice()))
                                .append(" VNĐ\n");
                        promotionData.append("  - Giá trẻ em: ").append(String.format("%,.0f", tour.getChildPrice()))
                                .append(" VNĐ\n");
                        promotionData.append("  - Đánh giá: 4.5/5 (10 đánh giá)\n");
                        promotionData.append("  - Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append("\n");
                        promotionData.append("  - Hot: ").append(tour.getIsHot() ? "Có" : "Không").append("\n\n");
                    }
                }

                String aiPrompt = String.format(
                        """
                                Bạn là chuyên gia phân tích khuyến mãi du lịch. Dựa trên yêu cầu: "%s"

                                Và danh sách tour khuyến mãi hiện có:
                                %s

                                Hãy phân tích và trả lời ngắn gọn:

                                🎉 **TOUR KHUYẾN MÃI HẤP DẪN**

                                Tôi đã tìm thấy %d tour đang có khuyến mãi hấp dẫn. Đây là cơ hội tuyệt vời để tiết kiệm chi phí:

                                ⚡ **Lý do nên chọn tour khuyến mãi:**
                                - Tiết kiệm chi phí đáng kể
                                - Chất lượng dịch vụ không đổi
                                - Cơ hội trải nghiệm tour cao cấp với giá tốt
                                - Thời gian khuyến mãi có hạn

                                🎯 **Tour khuyến mãi theo danh mục:**
                                - Du lịch biển: Tour nghỉ dưỡng, resort cao cấp
                                - Du lịch núi: Tour trekking, khám phá thiên nhiên
                                - Du lịch văn hóa: Tour lịch sử, di sản thế giới

                                💡 **Mẹo tận dụng khuyến mãi:**
                                - Đặt tour sớm để giữ giá tốt
                                - Kiểm tra điều kiện áp dụng
                                - So sánh giá trước và sau khuyến mãi
                                - Đặt tour theo nhóm để có ưu đãi thêm

                                📞 **Tư vấn tour khuyến mãi:** 1900-xxxx
                                - Cơ hội hiếm có, nên nắm bắt ngay
                                - Thường kèm theo nhiều ưu đãi khác

                                🚨 **LƯU Ý QUAN TRỌNG:**
                                - Khuyến mãi có thời hạn, nên đặt sớm
                                - Kiểm tra điều kiện áp dụng khuyến mãi
                                - So sánh giá trước và sau khuyến mãi
                                - Đặt tour ngay khi còn chỗ trống

                                💡 **GỢI Ý ĐẶT TOUR KHUYẾN MÃI:**
                                - Nên đặt ngay để không bỏ lỡ cơ hội
                                - Chọn thời gian phù hợp với lịch trình
                                - Đọc kỹ điều khoản và điều kiện
                                - Liên hệ để được tư vấn chi tiết
                                """,
                        userMessage, promotionData.toString());

                String aiResponse = model.chat(aiPrompt);
                response.append(aiResponse);
            } else {
                response.append("Hiện tại chưa có tour khuyến mãi nào. Vui lòng thử lại sau!");
            }

            return ChatResponse.builder()
                    .message(response.toString())
                    .sessionId("")
                    .responseType("promotion_tours")
                    .build();
        } catch (Exception e) {
            log.error("Error getting promotion tours: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi lấy tour khuyến mãi. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
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
        try {
            // Lấy thông tin tour chi tiết để AI có thể tham khảo
            Pageable pageable = PageRequest.of(0, 10);
            Page<TourSummaryProjection> tours = tourRepository.findAllActiveToursSummary(pageable);

            StringBuilder tourInfo = new StringBuilder();
            if (tours.hasContent()) {
                tourInfo.append("DANH SÁCH TOUR HIỆN CÓ:\n\n");
                for (TourSummaryProjection tour : tours.getContent()) {
                    tourInfo.append("🏷️ ").append(tour.getTitle()).append("\n");
                    tourInfo.append("   📍 Điểm đến: ").append(tour.getDestination()).append("\n");
                    tourInfo.append("   🚀 Khởi hành: ").append(tour.getDeparture()).append("\n");
                    tourInfo.append("   🏷️ Danh mục: ").append(tour.getCategoryName()).append("\n");
                    tourInfo.append("   ⏰ Thời gian: ").append(tour.getDuration()).append(" ngày\n");
                    tourInfo.append("   💰 Giá: ").append(String.format("%,.0f", tour.getAdultPrice()))
                            .append(" VNĐ\n");
                    tourInfo.append("   ⭐ Đánh giá: 4.5/5 (10 đánh giá)\n");
                    tourInfo.append("   🏆 Nổi bật: ").append(tour.getFeatured() ? "Có" : "Không").append(" | Hot: ")
                            .append(tour.getIsHot() ? "Có" : "Không").append(" | Khuyến mãi: ")
                            .append(tour.getHasPromotion() ? "Có" : "Không").append("\n\n");
                }
            }

            String prompt = String.format("""
                    Bạn là AI trợ lý tư vấn tour du lịch chuyên nghiệp với 10+ năm kinh nghiệm.
                    Bạn có kiến thức sâu rộng về du lịch Việt Nam và thế giới.

                    THÔNG TIN TOUR HIỆN CÓ:
                    %s

                    CÂU HỎI CỦA KHÁCH HÀNG: "%s"

                    Hãy trả lời một cách:
                    - Thân thiện, nhiệt tình và chuyên nghiệp
                    - Sử dụng emoji phù hợp để tạo cảm giác gần gũi
                    - Đưa ra gợi ý cụ thể về tour dựa trên dữ liệu có sẵn
                    - Phân tích và so sánh các tour phù hợp
                    - Đưa ra lời khuyên về thời gian, ngân sách, và lựa chọn
                    - Sử dụng thông tin thực tế từ danh sách tour
                    - Trả lời bằng tiếng Việt tự nhiên, dễ hiểu

                    Nếu câu hỏi không liên quan đến tour, hãy lịch sự chuyển hướng về chủ đề tour du lịch.
                    """, tourInfo.toString(), request.getMessage());

            String aiResponse = model.chat(prompt);

            return ChatResponse.builder()
                    .message(aiResponse)
                    .sessionId(request.getSessionId())
                    .responseType("general_query")
                    .build();
        } catch (Exception e) {
            log.error("Error handling general query: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi xử lý câu hỏi. Vui lòng thử lại sau.")
                    .sessionId(request.getSessionId())
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public String analyzeIntent(String userMessage, ChatModel model) {
        try {
            String prompt = String.format("""
                    Phân tích intent của câu hỏi sau và trả về một trong các giá trị sau:
                    - search_tour: Tìm kiếm tour
                    - check_schedule: Kiểm tra lịch trình
                    - tour_details: Chi tiết tour
                    - price_inquiry: Hỏi về giá
                    - availability_check: Kiểm tra chỗ trống
                    - category_tours: Tour theo danh mục
                    - featured_tours: Tour nổi bật
                    - hot_tours: Tour hot
                    - promotion_tours: Tour khuyến mãi
                    - booking_help: Hướng dẫn đặt tour
                    - tour_comparison: So sánh tour
                    - destination_info: Thông tin điểm đến
                    - duration_tours: Tour theo thời gian
                    - price_range_tours: Tour theo khoảng giá
                    - general_query: Câu hỏi chung

                    Câu hỏi: "%s"

                    Chỉ trả về tên intent, không giải thích gì khác.
                    """, userMessage);

            return model.chat(prompt).trim();
        } catch (Exception e) {
            log.error("Error analyzing intent with AI: {}", e.getMessage(), e);
            return "general_query";
        }
    }
}
