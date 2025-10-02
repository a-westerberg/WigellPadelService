package com.skrt.wigellpadelservice.dto.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingUpdateRequest(
        @NotNull UUID id,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @Min(1) int players
) {}
