package com.skrt.wigellpadelservice.dto.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;


public record BookingCreateRequest(
        @NotBlank String courtName,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @Min(1) int players
) {}
