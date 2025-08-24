package com.phenikaa.bookingService.mapper;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateBookingMapper {
    Booking toEntity(CreateBookingRequest dto);
}
