package com.phenikaa.bookingService.service.implement;

import com.phenikaa.bookingService.client.PromotionServiceClient;
import com.phenikaa.bookingService.client.TourServiceClient;
import com.phenikaa.bookingService.client.UserServiceClient;
import com.phenikaa.bookingService.dto.request.ApplyPromotionRequest;
import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.dto.response.AdminBookingResponse;
import com.phenikaa.bookingService.dto.response.BookingStatsResponse;
import com.phenikaa.bookingService.dto.response.PromotionValidationResponse;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import com.phenikaa.bookingService.mapper.CreateBookingMapper;
import com.phenikaa.bookingService.mapper.ViewBookingMapper;
import com.phenikaa.bookingService.repository.BookingRepository;
import com.phenikaa.bookingService.service.interfaces.BookingService;
import com.phenikaa.bookingService.service.interfaces.QrPaymentService;
import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.dto.response.ScheduleInfoResponse;
import com.phenikaa.bookingService.dto.response.UserInfoResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final CreateBookingMapper createBookingMapper;
    private final ViewBookingMapper viewBookingMapper;
    private final TourServiceClient tourServiceClient;
    private final UserServiceClient userServiceClient;
    private final PromotionServiceClient promotionServiceClient;
    private final QrPaymentService qrPaymentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Booking createBooking(Integer userId, Integer scheduleId, CreateBookingRequest dto) {
        // Add validation for null values
        if (dto.getAdultCount() == null) {
            dto.setAdultCount(0);
        }
        if (dto.getChildCount() == null) {
            dto.setChildCount(0);
        }

        // Validate that at least one person is booking
        if (dto.getAdultCount() + dto.getChildCount() <= 0) {
            throw new RuntimeException("Số lượng người đặt tour phải lớn hơn 0");
        }

        // Add validation for scheduleId
        GetInfoTour request = tourServiceClient.getInfoTour(scheduleId);
        Booking booking = createBookingMapper.toEntity(dto);
        booking.setUserId(userId);
        booking.setScheduleId(scheduleId);

        // Generate booking code
        booking.setBookingCode(generateBookingCode());

        // Set default status
        booking.setStatus(BookingStatus.PENDING);

        // Tính total amount
        Double totalAmount = dto.getAdultCount() * request.getAdultPrice()
                + dto.getChildCount() * request.getChildPrice();
        booking.setTotalAmount(totalAmount);

        // Set default final amount (without promotion)
        booking.setFinalAmount(totalAmount);
        booking.setDiscountAmount(0.0);

        // Xử lý promotion code nếu có (trước khi lưu booking)
        if (dto.getPromotionCode() != null && !dto.getPromotionCode().trim().isEmpty()) {
            try {
                System.out.println("=== PROMOTION VALIDATION DEBUG ===");
                System.out.println("PromotionCode: " + dto.getPromotionCode());
                System.out.println("UserId: " + userId);
                System.out.println("TotalAmount: " + totalAmount);

                // Validate promotion code trước
                PromotionValidationResponse validation = promotionServiceClient.validatePromotionCode(
                        dto.getPromotionCode(),
                        userId,
                        totalAmount).getBody();

                if (validation != null && validation.isValid()) {
                    // Cập nhật booking với thông tin promotion
                    booking.setPromotionCode(dto.getPromotionCode());
                    booking.setDiscountAmount(validation.getDiscountAmount());
                    booking.setFinalAmount(validation.getFinalAmount());
                } else {
                    throw new RuntimeException("Invalid promotion code: " +
                            (validation != null ? validation.getMessage() : "Unknown error"));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating promotion: " + e.getMessage());
            }
        }

        // Thiết lập tham chiếu thanh toán và QR URL trước khi trả về
        booking.setPaymentReference(booking.getBookingCode());
        String qrUrl = qrPaymentService.generateQrUrl(booking.getBookingCode(), booking.getFinalAmount());
        booking.setQrUrl(qrUrl);

        // Lưu booking với tất cả thông tin (bao gồm promotion và QR nếu có)
        Booking savedBooking = bookingRepository.save(booking);

        // Apply promotion code sau khi đã có bookingId (để tăng used_count và lưu usage
        // record)
        if (dto.getPromotionCode() != null && !dto.getPromotionCode().trim().isEmpty()) {
            try {
                System.out.println("=== PROMOTION APPLICATION DEBUG ===");
                System.out.println("Applying promotion for bookingId: " + savedBooking.getBookingId());

                ApplyPromotionRequest applyRequest = ApplyPromotionRequest.builder()
                        .promotionCode(dto.getPromotionCode())
                        .userId(userId)
                        .bookingId(savedBooking.getBookingId())
                        .orderAmount(totalAmount)
                        .build();

                // Gọi applyPromotionCode để tăng used_count và lưu usage record
                ResponseEntity<String> response = promotionServiceClient.applyPromotionCode(applyRequest);
                System.out.println("Promotion application response: " + response.getBody());
                System.out.println("Promotion application status: " + response.getStatusCode());
            } catch (Exception e) {
                throw new RuntimeException("Error applying promotion: " + e.getMessage());
            }
        }

        if (request.getAvailableSlots() >= (savedBooking.getAdultCount() + savedBooking.getChildCount())) {
            tourServiceClient.updateSchedule(scheduleId,
                    request.getAvailableSlots() - savedBooking.getAdultCount() - savedBooking.getChildCount());
            return savedBooking;
        } else {
            throw new RuntimeException("Not enough slots");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        return bookingPage.map(this::mapToAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> getAllBookings(Pageable pageable, String search, String status) {
        BookingStatus bookingStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If status is invalid, ignore it and search without status filter
                System.out.println("Invalid status: " + status);
            }
        }

        Page<Booking> bookingPage = bookingRepository.findBySearchAndStatus(search, bookingStatus, pageable);
        return bookingPage.map(this::mapToAdminResponse);
    }

    private AdminBookingResponse mapToAdminResponse(Booking booking) {
        AdminBookingResponse.AdminBookingResponseBuilder builder = AdminBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .scheduleId(booking.getScheduleId())
                .adultCount(booking.getAdultCount())
                .childCount(booking.getChildCount())
                .totalAmount(booking.getTotalAmount())
                .finalAmount(booking.getFinalAmount())
                .discountAmount(booking.getDiscountAmount())
                .promotionCode(booking.getPromotionCode())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        // Lấy thông tin customer
        try {
            if (booking.getUserId() != null) {
                UserInfoResponse userInfo = userServiceClient.getUserInfo(booking.getUserId());
                if (userInfo != null) {
                    builder.customerName(userInfo.getFullName())
                            .customerEmail(userInfo.getEmail())
                            .customerPhone(userInfo.getPhoneNumber());
                }
            }
        } catch (Exception e) {
            System.out
                    .println("Error fetching user info for booking " + booking.getBookingId() + ": " + e.getMessage());
            builder.customerName("N/A")
                    .customerEmail("N/A")
                    .customerPhone("N/A");
        }

        // Lấy thông tin tour
        try {
            if (booking.getScheduleId() != null) {
                // Lấy thông tin schedule
                ScheduleInfoResponse schedule = tourServiceClient.getSchedule(booking.getScheduleId());
                if (schedule != null) {
                    builder.tourName(schedule.getTourTitle())
                            .tourLocation("N/A") // Không có trong ScheduleInfoResponse
                            .tourPrice(0.0) // Không có trong ScheduleInfoResponse
                            .tourTitle(schedule.getTourTitle())
                            .tourDescription(schedule.getTourDescription())
                            .departureDate(schedule.getDepartureDate())
                            .returnDate(schedule.getReturnDate());
                }
            }
        } catch (Exception e) {
            System.out
                    .println("Error fetching tour info for booking " + booking.getBookingId() + ": " + e.getMessage());
            builder.tourName("N/A")
                    .tourLocation("N/A")
                    .tourPrice(0.0)
                    .tourTitle("N/A")
                    .tourDescription("N/A");
        }

        return builder.build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Booking updateBookingStatus(Integer bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Validate status transition
        validateStatusTransition(booking.getStatus(), status);

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        // Nếu booking đã bị hủy hoặc đã hoàn thành thì không thể thay đổi trạng thái
        if (currentStatus == BookingStatus.CANCELLED || currentStatus == BookingStatus.COMPLETED) {
            throw new RuntimeException("Không thể thay đổi trạng thái của booking đã " +
                    (currentStatus == BookingStatus.CANCELLED ? "bị hủy" : "hoàn thành"));
        }

        // Validate các transition hợp lệ
        switch (currentStatus) {
            case PENDING:
                // Từ PENDING có thể chuyển sang CONFIRMED hoặc CANCELLED
                if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.CANCELLED) {
                    throw new RuntimeException("Từ trạng thái PENDING chỉ có thể chuyển sang CONFIRMED hoặc CANCELLED");
                }
                break;
            case CONFIRMED:
                // Từ CONFIRMED có thể chuyển sang COMPLETED hoặc CANCELLED
                if (newStatus != BookingStatus.COMPLETED && newStatus != BookingStatus.CANCELLED) {
                    throw new RuntimeException(
                            "Từ trạng thái CONFIRMED chỉ có thể chuyển sang COMPLETED hoặc CANCELLED");
                }
                break;
            case CANCELLED:
                // Từ CANCELLED không thể chuyển sang trạng thái khác
                throw new RuntimeException("Không thể thay đổi trạng thái của booking đã bị hủy");
            case COMPLETED:
                // Từ COMPLETED không thể chuyển sang trạng thái khác
                throw new RuntimeException("Không thể thay đổi trạng thái của booking đã hoàn thành");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViewBookingResponse> getUserBookings(Integer userId, Pageable pageable) {
        Page<Booking> userBookings = bookingRepository.findByUserId(userId, pageable);
        return userBookings.map(booking -> {
            ViewBookingResponse response = viewBookingMapper.toDto(booking);

            // Lấy thông tin tour và schedule
            try {
                // Lấy thông tin schedule để có departureDate và returnDate
                ScheduleInfoResponse schedule = tourServiceClient.getSchedule(booking.getScheduleId());
                if (schedule != null) {
                    response.setDepartureDate(schedule.getDepartureDate());
                    response.setReturnDate(schedule.getReturnDate());
                    response.setTourTitle(schedule.getTourTitle());
                    response.setTourDescription(schedule.getTourDescription());
                }
            } catch (Exception e) {
                System.out.println(
                        "Error fetching tour info for booking " + booking.getBookingId() + ": " + e.getMessage());
                // Fallback values
                response.setTourTitle("Tour " + booking.getScheduleId());
                response.setTourDescription("Tour description not available");
            }

            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ViewBookingResponse getUserBookingDetail(Integer userId, Integer bookingId) {
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found with id: " + bookingId + " for user: " + userId);
        }

        ViewBookingResponse response = viewBookingMapper.toDto(booking);

        // Lấy thông tin tour và schedule
        try {
            // Lấy thông tin schedule để có departureDate và returnDate
            ScheduleInfoResponse schedule = tourServiceClient.getSchedule(booking.getScheduleId());
            if (schedule != null) {
                response.setDepartureDate(schedule.getDepartureDate());
                response.setReturnDate(schedule.getReturnDate());
                response.setTourTitle(schedule.getTourTitle());
                response.setTourDescription(schedule.getTourDescription());
            }
        } catch (Exception e) {
            System.out
                    .println("Error fetching tour info for booking " + booking.getBookingId() + ": " + e.getMessage());
            // Fallback values
            response.setTourTitle("Tour " + booking.getScheduleId());
            response.setTourDescription("Tour description not available");
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Booking cancelUserBooking(Integer userId, Integer bookingId) {
        // Tìm booking của user
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId);
        if (booking == null) {
            throw new EntityNotFoundException(
                    "Booking không tìm thấy với ID: " + bookingId + " cho người dùng: " + userId);
        }

        // Kiểm tra trạng thái có thể hủy không
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking đã được hủy trước đó");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy booking đã hoàn thành");
        }

        // Cập nhật trạng thái thành CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);

        // Hoàn trả slot cho tour (optional - có thể thêm logic này nếu cần)
        try {
            // Lấy thông tin tour để hoàn trả slot
            GetInfoTour tourInfo = tourServiceClient.getInfoTour(booking.getScheduleId());
            int totalPeople = booking.getAdultCount() + booking.getChildCount();
            tourServiceClient.updateSchedule(booking.getScheduleId(), tourInfo.getAvailableSlots() + totalPeople);
        } catch (Exception e) {
            // Log lỗi nhưng vẫn tiếp tục hủy booking
            System.err.println("Lỗi khi hoàn trả slot: " + e.getMessage());
        }

        return bookingRepository.save(booking);
    }

    /**
     * Generate unique booking code
     * Format: BK + sequential number (last booking + 1)
     * Example: BK1, BK2, BK3, ...
     */
    private String generateBookingCode() {
        // Get the last booking to find the highest booking code number
        // Use findAll with Pageable to get the most recent booking
        Page<Booking> lastBookingPage = bookingRepository.findAll(
                PageRequest.of(0, 1, Sort.by("bookingId").descending()));

        int nextNumber = 1; // Default to 1 if no bookings exist

        if (!lastBookingPage.isEmpty()) {
            Booking lastBooking = lastBookingPage.getContent().get(0);
            if (lastBooking.getBookingCode() != null) {
                try {
                    // Extract number from booking code (e.g., "BK123" -> 123)
                    String code = lastBooking.getBookingCode();
                    if (code.startsWith("BK")) {
                        String numberPart = code.substring(2);
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    }
                } catch (NumberFormatException e) {
                    // If parsing fails, start from 1
                    nextNumber = 1;
                }
            }
        }

        return "BK" + nextNumber;
    }

    @Override
    public BookingStatsResponse getBookingStats() {
        Long totalBookings = bookingRepository.count();
        Long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        Long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        Long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);

        return BookingStatsResponse.builder()
                .totalBookings(totalBookings)
                .confirmed(confirmed)
                .pending(pending)
                .cancelled(cancelled)
                .refunded(0L) // Not available in current enum
                .completed(completed)
                .build();
    }

    @Override
    public Long getTourBookedCount(Integer tourId) {
        try {
            // Bước 1: Lấy danh sách schedule IDs của tour từ tour service
            List<Integer> scheduleIds = getScheduleIdsForTour(tourId);

            if (scheduleIds.isEmpty()) {
                System.out.println("No schedules found for tourId: " + tourId);
                return 0L;
            }

            // Bước 2: Đếm bookings theo schedule IDs
            Long count = bookingRepository.countByScheduleIdsAndValidStatus(scheduleIds);
            return count != null ? count : 0L;
        } catch (Exception e) {
            System.err.println("Error getting tour booked count for tourId " + tourId + ": " + e.getMessage());
            return 0L;
        }
    }

    private List<Integer> getScheduleIdsForTour(Integer tourId) {
        try {
            System.out.println("Getting schedule IDs for tourId: " + tourId);

            // Gọi tour service để lấy danh sách schedules
            List<ScheduleInfoResponse> schedules = tourServiceClient.getAllSchedules(tourId);

            if (schedules == null || schedules.isEmpty()) {
                System.out.println("No schedules found for tourId: " + tourId);
                return new ArrayList<>();
            }

            // Extract schedule IDs
            List<Integer> scheduleIds = schedules.stream()
                    .map(ScheduleInfoResponse::getScheduleId)
                    .collect(java.util.stream.Collectors.toList());

            System.out.println("Found " + scheduleIds.size() + " schedules for tourId: " + tourId);
            System.out.println("Schedule IDs: " + scheduleIds);

            return scheduleIds;
        } catch (Exception e) {
            System.err.println("Error getting schedule IDs for tourId " + tourId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
