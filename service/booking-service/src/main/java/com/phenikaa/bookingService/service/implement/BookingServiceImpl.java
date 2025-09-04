package com.phenikaa.bookingService.service.implement;

import com.phenikaa.bookingService.client.TourServiceClient;
import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import com.phenikaa.bookingService.mapper.CreateBookingMapper;
import com.phenikaa.bookingService.mapper.ViewBookingMapper;
import com.phenikaa.bookingService.repository.BookingRepository;
import com.phenikaa.bookingService.service.interfaces.BookingService;
import com.phenikaa.dto.response.GetInfoTour;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final CreateBookingMapper createBookingMapper;
    private final ViewBookingMapper viewBookingMapper;
    private final TourServiceClient tourServiceClient;

    @Override
    public Booking createBooking(Integer userId, Integer scheduleId , CreateBookingRequest dto) {
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
        booking.setTotalAmount(dto.getAdultCount() * request.getAdultPrice() + dto.getChildCount() * request.getChildPrice());
        if(request.getAvailableSlots() >= (dto.getAdultCount() + dto.getChildCount())) {
            tourServiceClient.updateSchedule(scheduleId, request.getAvailableSlots() - booking.getAdultCount() - booking.getChildCount());
            return bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Not enough slots");
        }
    }

    @Override
    public Page<ViewBookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        return bookingPage.map(viewBookingMapper::toDto);
    }

    @Override
    public void deleteBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        bookingRepository.delete(booking);
    }

    @Override
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
                    throw new RuntimeException("Từ trạng thái CONFIRMED chỉ có thể chuyển sang COMPLETED hoặc CANCELLED");
                }
                break;
        }
    }

    @Override
    public Page<ViewBookingResponse> getUserBookings(Integer userId, Pageable pageable) {
        Page<Booking> userBookings = bookingRepository.findByUserId(userId, pageable);
        return userBookings.map(viewBookingMapper::toDto);
    }

    @Override
    public ViewBookingResponse getUserBookingDetail(Integer userId, Integer bookingId) {
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found with id: " + bookingId + " for user: " + userId);
        }
        return viewBookingMapper.toDto(booking);
    }

    @Override
    public Booking cancelUserBooking(Integer userId, Integer bookingId) {
        // Tìm booking của user
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking không tìm thấy với ID: " + bookingId + " cho người dùng: " + userId);
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
}
