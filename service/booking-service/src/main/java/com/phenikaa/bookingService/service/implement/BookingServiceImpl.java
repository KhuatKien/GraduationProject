package com.phenikaa.bookingService.service.implement;

import com.phenikaa.bookingService.client.TourServiceClient;
import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
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
}
