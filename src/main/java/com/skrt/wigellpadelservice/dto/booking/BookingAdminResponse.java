package com.skrt.wigellpadelservice.dto.booking;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingAdminResponse(
        String id,
        String customerId,
        String courtId,
        String courtName,
        LocalDate date,
        LocalTime time,
        int players,
        BigDecimal totalPriceSek,
        BigDecimal totalPriceEur,
        boolean canceled,
        LocalDateTime canceledAt
) {}
