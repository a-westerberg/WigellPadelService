package com.skrt.wigellpadelservice.mappers;

import com.skrt.wigellpadelservice.dto.booking.BookingAdminResponse;
import com.skrt.wigellpadelservice.dto.booking.BookingResponse;
import com.skrt.wigellpadelservice.entities.PadelBooking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BookingMapper {

    public BookingResponse toResponse(PadelBooking booking) {
        return toResponse(booking,null);
    }

    public BookingResponse toResponse(PadelBooking booking, BigDecimal eur) {
        String id = toString(booking.getId());
        String customerId = (booking.getCustomer() != null) ? toString(booking.getCustomer().getId()) : null;
        String courtId = (booking.getCourt() != null) ? toString(booking.getCourt().getId()) : null;
        String courtName = (booking.getCourt() != null) ? booking.getCourt().getName() : null;

        return new BookingResponse(
                id,
                customerId,
                courtId,
                courtName,
                booking.getDate(),
                booking.getTime(),
                booking.getPlayers(),
                booking.getTotalPriceSek(),
                eur
        );
    }

    public BookingAdminResponse toAdminResponse(PadelBooking booking) {
        return toAdminResponse(booking, null);
    }

    public BookingAdminResponse toAdminResponse(PadelBooking booking, BigDecimal eur) {
        String id = toString(booking.getId());
        String customerId = (booking.getCustomer() != null) ? toString(booking.getCustomer().getId()) : null;
        String courtId = (booking.getCourt() != null) ? toString(booking.getCourt().getId()) : null;
        String courtName = (booking.getCourt() != null) ? booking.getCourt().getName() : null;

        return new BookingAdminResponse(
                id,
                customerId,
                courtId,
                courtName,
                booking.getDate(),
                booking.getTime(),
                booking.getPlayers(),
                booking.getTotalPriceSek(),
                eur,
                booking.isCanceled(),
                booking.getCanceledAt()
        );
    }

    private static String toString(UUID id) {
        return id != null ? id.toString() : null;
    }
}
