package com.skrt.wigellpadelservice.repositories;

import com.skrt.wigellpadelservice.entities.PadelCourt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PadelCourtRepository extends JpaRepository<PadelCourt, UUID> {

    Optional<PadelCourt> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<PadelCourt> findAllByOrderByNameAsc();

}
