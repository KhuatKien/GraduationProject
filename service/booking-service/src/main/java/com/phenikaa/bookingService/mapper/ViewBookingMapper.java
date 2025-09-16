package com.phenikaa.bookingService.mapper;

import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ViewBookingMapper {
    ViewBookingResponse toDto(Booking booking);
}
