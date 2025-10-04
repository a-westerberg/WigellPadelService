package com.skrt.wigellpadelservice.controllers;

import com.skrt.wigellpadelservice.dto.booking.*;
import com.skrt.wigellpadelservice.entities.PadelBooking;
import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.exceptions.ResourceNotFoundException;
import com.skrt.wigellpadelservice.mappers.BookingMapper;
import com.skrt.wigellpadelservice.services.CurrencyService;
import com.skrt.wigellpadelservice.services.PadelBookingService;
import com.skrt.wigellpadelservice.services.PadelCourtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wigellpadel")
@Validated
public class PadelBookingController {

    private final PadelBookingService bookingService;
    private final PadelCourtService courtService;
    private final BookingMapper bookingMapper;
    private final CurrencyService currencyService;

    @Autowired
    public PadelBookingController(PadelBookingService bookingService, PadelCourtService courtService, BookingMapper bookingMapper, CurrencyService currencyService) {
        this.bookingService = bookingService;
        this.courtService = courtService;
        this.bookingMapper = bookingMapper;
        this.currencyService = currencyService;
    }

    @GetMapping("/checkavailability/{courtId}/{date}")
    public ResponseEntity<AvailabilityResponse> checkAvailability(@PathVariable UUID courtId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> free = bookingService.getAvailableSlots(courtId, date);

        PadelCourt court = courtService.getCourtById(courtId)
                .orElseThrow(()-> new ResourceNotFoundException("court", courtId));

        AvailabilityResponse response = new AvailabilityResponse(
                courtId.toString(),
                court.getName(),
                court.getMaxPlayers(),
                date,
                free
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/booking/bookcourt")
    public ResponseEntity<BookingResponse> bookCourt(@Valid @RequestBody BookingCreateRequest request){
        PadelBooking saved = bookingService.bookCourt(
                request.courtName(),
                request.date(),
                request.time(),
                request.players()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toResponse(saved, currencyService.toEur(saved.getTotalPriceSek())));
    }

    @GetMapping("/v1/mybookings")
    public ResponseEntity<List<BookingResponse>> myBookings(){
        List<BookingResponse> body = bookingService.myBookings()
                .stream()
                .map(b -> bookingMapper.toResponse(b, currencyService.toEur(b.getTotalPriceSek())))
                .toList();
        return ResponseEntity.ok(body);
    }

    @PutMapping("/v1/updatebooking")
    public ResponseEntity<BookingResponse> updateBooking(@Valid @RequestBody BookingUpdateRequest request){
        PadelBooking updated = bookingService.updateBooking(
                request.id(),
                request.date(),
                request.time(),
                request.players()
        );
        return ResponseEntity.ok(bookingMapper.toResponse(updated, currencyService.toEur(updated.getTotalPriceSek())));
    }

    @DeleteMapping("/v1/cancelbooking")
    public ResponseEntity<Void> cancelBooking(@RequestParam UUID id){
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/listcanceled")
    public ResponseEntity<List<BookingAdminResponse>> listCanceled(){
        List<BookingAdminResponse> body = bookingService.listCanceled()
                .stream()
                .map(b -> bookingMapper.toAdminResponse(b, currencyService.toEur(b.getTotalPriceSek())))
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/v1/listupcoming")
    public ResponseEntity<List<BookingAdminResponse>> listUpcoming(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate today){
        List<BookingAdminResponse> body = bookingService.listUpcoming(today)
                .stream()
                .map(b -> bookingMapper.toAdminResponse(b, currencyService.toEur(b.getTotalPriceSek())))
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/v1/listpast")
    public ResponseEntity<List<BookingAdminResponse>> listPast(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate today){
        List<BookingAdminResponse> body = bookingService.listPast(today)
                .stream()
                .map(b -> bookingMapper.toAdminResponse(b, currencyService.toEur(b.getTotalPriceSek())))
                .toList();
        return ResponseEntity.ok(body);
    }
}

