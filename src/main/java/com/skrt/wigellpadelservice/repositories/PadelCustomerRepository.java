package com.skrt.wigellpadelservice.repositories;

import com.skrt.wigellpadelservice.entities.PadelCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PadelCustomerRepository extends JpaRepository<PadelCustomer, UUID> {

    Optional<PadelCustomer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
