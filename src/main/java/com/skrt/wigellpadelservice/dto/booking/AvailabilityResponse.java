package com.skrt.wigellpadelservice.dto.booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        String courtId,
        String courtName,
        int maxPlayers,
        LocalDate date,
        List<LocalTime> availableSlots
) {}
