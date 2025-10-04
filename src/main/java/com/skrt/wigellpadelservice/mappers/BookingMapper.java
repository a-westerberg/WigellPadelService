package com.skrt.wigellpadelservice.mappers;

import com.skrt.wigellpadelservice.dto.booking.BookingAdminResponse;
import com.skrt.wigellpadelservice.dto.booking.BookingResponse;
import com.skrt.wigellpadelservice.entities.PadelBooking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookingMapper {

    public BookingResponse toResponse(PadelBooking booking, BigDecimal eur) {
        String id = toStringSafe(booking.getId());
        String customerId = (booking.getCustomer() != null) ? toStringSafe(booking.getCustomer().getId()) : null;
        String courtId = (booking.getCourt() != null) ? toStringSafe(booking.getCourt().getId()) : null;
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

    public BookingAdminResponse toAdminResponse(PadelBooking booking, BigDecimal eur) {
        String id = toStringSafe(booking.getId());
        String customerId = (booking.getCustomer() != null) ? toStringSafe(booking.getCustomer().getId()) : null;
        String courtId = (booking.getCourt() != null) ? toStringSafe(booking.getCourt().getId()) : null;
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

    private static String toStringSafe(Object id) {
        return id != null ? id.toString() : null;
    }
}
