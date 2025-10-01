package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class SlotUnavailableException extends RuntimeException {

    private final UUID courtId;
    private final LocalDate date;
    private final LocalTime time;

    public SlotUnavailableException(UUID courtId, LocalDate date, LocalTime time) {
        super("Slot not available: courtId=%s, date=%s, time=%s".formatted(courtId, date, time));
        this.courtId = courtId;
        this.date = date;
        this.time = time;
    }

    public UUID getCourtId() {
        return courtId;
    }
    public LocalDate getDate() {
        return date;
    }
    public LocalTime getTime() {
        return time;
    }
}
