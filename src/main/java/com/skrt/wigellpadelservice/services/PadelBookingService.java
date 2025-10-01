package com.skrt.wigellpadelservice.services;

import com.skrt.wigellpadelservice.entities.PadelBooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface PadelBookingService {

    boolean isSlotFree(UUID courtId, LocalDate date, LocalTime time);

    List<LocalTime> getAvailableSlots(UUID courtId, LocalDate date);

    PadelBooking bookCourt(UUID customerId,UUID courtId, LocalDate date, LocalTime time, int players);

    List<PadelBooking> myBookings(UUID customerId);

    PadelBooking updateBooking(UUID bookingId, LocalDate newDate, LocalTime newTime, int newPlayers);

    void cancelBooking(UUID bookingId);

    List<PadelBooking> listCanceled();

    List<PadelBooking> listUpcoming(LocalDate today);

    List<PadelBooking> listPast(LocalDate today);
}
