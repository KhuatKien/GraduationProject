//package com.phenikaa.bookingService.mapper;
//
//import com.phenikaa.bookingService.dto.response.RefundResponse;
//import com.phenikaa.bookingService.entity.Booking;
//import com.phenikaa.bookingService.entity.Refund;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface RefundMapper {
//
//    @Mapping(source = "refund.refundId", target = "refundId")
//    @Mapping(source = "refund.refundCode", target = "refundCode")
//    @Mapping(source = "refund.bookingId", target = "bookingId")
//    @Mapping(source = "booking.bookingCode", target = "bookingCode")
//    @Mapping(source = "refund.refundAmount", target = "refundAmount")
//    @Mapping(source = "refund.reason", target = "reason")
//    @Mapping(source = "refund.status", target = "status")
//    @Mapping(source = "refund.adminNote", target = "adminNote")
//    @Mapping(source = "refund.processedBy", target = "processedBy")
//    @Mapping(source = "refund.createdAt", target = "createdAt")
//    @Mapping(source = "refund.updatedAt", target = "updatedAt")
//    @Mapping(source = "refund.processedAt", target = "processedAt")
//    @Mapping(source = "booking.userId", target = "userId")
//    @Mapping(source = "booking.scheduleId", target = "scheduleId")
//    @Mapping(source = "booking.totalAmount", target = "originalAmount")
//    RefundResponse toDto(Refund refund, Booking booking);
//}
