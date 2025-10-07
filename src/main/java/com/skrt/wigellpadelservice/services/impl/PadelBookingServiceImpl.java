package com.skrt.wigellpadelservice.services.impl;

import com.skrt.wigellpadelservice.entities.PadelBooking;
import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.entities.PadelCustomer;
import com.skrt.wigellpadelservice.exceptions.*;
import com.skrt.wigellpadelservice.repositories.PadelBookingRepository;
import com.skrt.wigellpadelservice.repositories.PadelCourtRepository;
import com.skrt.wigellpadelservice.repositories.PadelCustomerRepository;
import com.skrt.wigellpadelservice.services.PadelBookingService;
import com.skrt.wigellpadelservice.services.PadelPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.skrt.wigellpadelservice.utility.Validate.*;
import static com.skrt.wigellpadelservice.utility.SecurityUtil.*;

@Service
@Transactional
public class PadelBookingServiceImpl implements PadelBookingService {

    private static final Logger logger = LoggerFactory.getLogger(PadelBookingServiceImpl.class);

    private static final List<LocalTime> DEFAULT_SLOTS = IntStream.rangeClosed(7,20).mapToObj(h -> LocalTime.of(h,0)).toList();
    private static final ZoneId ZONE_SE = ZoneId.of("Europe/Stockholm");

    private final PadelBookingRepository bookingRepo;
    private final PadelCourtRepository courtRepo;
    private final PadelCustomerRepository customerRepo;
    private final PadelPricingService pricingService;

    @Autowired
    public PadelBookingServiceImpl(PadelBookingRepository bookingRepo, PadelCourtRepository courtRepo, PadelCustomerRepository customerRepo, PadelPricingService pricingService) {
        this.bookingRepo = bookingRepo;
        this.courtRepo = courtRepo;
        this.customerRepo = customerRepo;
        this.pricingService = pricingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID courtId, LocalDate date) {
        notNull(courtId, "courtId");
        notNull(date, "date");

        List<PadelBooking> bookings =
                bookingRepo.findByCourtIdAndDateAndCanceledFalseOrderByTimeAsc(courtId, date);
        Set<LocalTime> slots = bookings.stream()
                .map(PadelBooking::getTime)
                .collect(Collectors.toSet());

        return DEFAULT_SLOTS.stream()
                .filter(slotStart -> !slots.contains(slotStart))
                .toList();
    }

    @Override
    public PadelBooking bookCourt(String courtName, LocalDate date, LocalTime time, int players) {
        if(courtName == null || courtName.isBlank()){
            throw new BadRequestException("courtName", "is required");
        }

        notNull(date,  "date");
        notNull(time,  "time");
        positive(players, "players");

        Long customerId = resolveCurrentCustomerId();

        PadelCustomer customer = customerRepo.findById(customerId)
                .orElseThrow(()-> new ResourceNotFoundException("customer", customerId));

        PadelCourt court = courtRepo.findByNameIgnoreCase(courtName.trim())
                .filter(PadelCourt::isActive)
                .orElseThrow(()-> new ResourceNotFoundException("active court", courtName));

        isTrue(players <= court.getMaxPlayers(),
        () ->new BadRequestException("players", "exceeds court max", players));

        isTrue(bookingRepo.isSlotFree(court.getId(),date,time),
                () -> new SlotUnavailableException(court.getId(), date, time));

        PadelBooking booking = new PadelBooking();
        booking.setCustomer(customer);
        booking.setCourt(court);
        booking.setDate(date);
        booking.setTime(time);
        booking.setPlayers(players);
        booking.setTotalPriceSek(pricingService.calculatePriceSek(players));

        PadelBooking saved = bookingRepo.save(booking);

        logger.info("user '{}' booked {} players to court '{}' on {} {} (bookingId={})",
        currentUsername(), players, court.getName(), date, time, saved.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PadelBooking> myBookings() {
        Long customerId = resolveCurrentCustomerId();
        return bookingRepo.findByCustomerIdAndCanceledFalseOrderByDateDesc(customerId);
    }

    @Override
    public PadelBooking updateBooking(UUID bookingId, LocalDate newDate, LocalTime newTime, int newPlayers) {
        notNull(bookingId, "bookingId");
        notNull(newDate, "date");
        notNull(newTime, "time");
        positive(newPlayers, "players");

        PadelBooking existingBooking = bookingRepo.findByIdAndCanceledFalse(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("active booking", bookingId));

        assertOwnerOrAdmin(
                existingBooking.getCustomer().getId(),
                resolveCurrentCustomerId(),
                new ForbiddenOperationException("modify", "booking", bookingId.toString())
        );

        PadelCourt court = existingBooking.getCourt();

        isTrue(newPlayers <= court.getMaxPlayers(),
                ()-> new BadRequestException("players", "exceeds court max", newPlayers));

        boolean slotChanged = !Objects.equals(existingBooking.getDate(), newDate)
                        || !Objects.equals(existingBooking.getTime(), newTime);

        if (slotChanged) {
            isTrue(bookingRepo.isSlotFree(court.getId(), newDate, newTime),
                    () -> new SlotUnavailableException(court.getId(), newDate, newTime));
        }

        existingBooking.setDate(newDate);
        existingBooking.setTime(newTime);
        existingBooking.setPlayers(newPlayers);
        existingBooking.setTotalPriceSek(pricingService.calculatePriceSek(newPlayers));

        PadelBooking savedBooking = bookingRepo.save(existingBooking);

        logger.info("booking updated by '{}': id={}, court='{}', date={}, time={}, players={}",
                currentUsername(), savedBooking.getId(), court.getName(), savedBooking.getDate(), savedBooking.getTime(), savedBooking.getPlayers());

        return savedBooking;
    }

    @Override
    public void cancelBooking(UUID bookingId) {
        notNull(bookingId, "bookingId");

        PadelBooking existing = bookingRepo.findByIdAndCanceledFalse(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("active booking", bookingId));

        assertOwnerOrAdmin(
                existing.getCustomer().getId(),
                resolveCurrentCustomerId(),
                new ForbiddenOperationException("cancel", "booking", bookingId.toString())
        );

        LocalDate today = LocalDate.now(ZONE_SE);
        LocalDate limit = existing.getDate().minusDays(7);

        if(!today.isBefore(limit)) {
            throw new CancellationNotAllowedException(bookingId,existing.getDate(), today, 7);
        }

        existing.setCanceled(true);
        bookingRepo.save(existing);

        logger.info("booking canceled by '{}': id={}, court='{}', date={}, time={}",
                currentUsername(), existing.getId(), existing.getCourt().getName(), existing.getDate(), existing.getTime());

    }

    @Override
    @Transactional(readOnly = true)
    public List<PadelBooking> listCanceled() {
        return bookingRepo.findByCanceledTrueOrderByCanceledAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PadelBooking> listUpcoming(LocalDate today) {
        if(today == null) today = LocalDate.now(ZONE_SE);
        return bookingRepo.findByDateAfterAndCanceledFalseOrderByDateAsc(today);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PadelBooking> listPast(LocalDate today) {
        if(today == null) today = LocalDate.now(ZONE_SE);
        return bookingRepo.findByDateBeforeOrderByDateDesc(today);
    }

    private Long resolveCurrentCustomerId() {
        String username = currentUsername();
        return customerRepo.findByNameIgnoreCase(username)
                .map(PadelCustomer::getId)
                .orElseGet(() -> {
                    PadelCustomer created = new PadelCustomer();
                    created.setName(username);
                    return customerRepo.save(created).getId();
                });
    }

}
