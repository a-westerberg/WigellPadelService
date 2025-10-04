package com.skrt.wigellpadelservice.repositories;

import com.skrt.wigellpadelservice.entities.PadelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PadelBookingRepository extends JpaRepository<PadelBooking, UUID> {

    @Query("""
            select (count(b) = 0)
            from PadelBooking b
            where b.court.id = :courtId
            and b.date = :date
            and b.time = :time
            and b.canceled = false
            """)
    boolean isSlotFree(@Param("courtId") UUID courtId,
                       @Param("date") LocalDate date,
                       @Param("time") LocalTime time);

    List<PadelBooking> findByCourtIdAndDateAndCanceledFalseOrderByTimeAsc(UUID courtId, LocalDate date);

    List<PadelBooking> findByCustomerIdAndCanceledFalseOrderByDateDesc(Long customerId);

    List<PadelBooking> findByCanceledTrueOrderByCanceledAtDesc();
    List<PadelBooking> findByDateAfterAndCanceledFalseOrderByDateAsc(LocalDate date);
    List<PadelBooking> findByDateBeforeAndCanceledFalseOrderByDateDesc(LocalDate date);

    Optional<PadelBooking> findByIdAndCanceledFalse(UUID id);


}
