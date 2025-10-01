package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class CancellationNotAllowedException extends RuntimeException {

    private final UUID bookingId;
    private final LocalDate bookingDate;
    private final LocalDate today;
    private final int minDays;

    public CancellationNotAllowedException(UUID bookingId, LocalDate bookingDate, LocalDate today, int minDays) {
        super("Cancellation not allowed (min %d days): bookingId=%s, bookingDate=%s, today=%s".formatted(minDays, bookingId, bookingDate, today));
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.today = today;
        this.minDays = minDays;
    }

    public UUID getBookingId() {
        return bookingId;
    }
    public LocalDate getBookingDate() {
        return bookingDate;
    }
    public LocalDate getToday() {
        return today;
    }
    public int getMinDays() {
        return minDays;
    }
}
