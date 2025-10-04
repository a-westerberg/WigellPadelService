package com.skrt.wigellpadelservice.repositories;

import com.skrt.wigellpadelservice.entities.PadelCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PadelCustomerRepository extends JpaRepository<PadelCustomer, Long> {

    Optional<PadelCustomer> findByNameIgnoreCase(String name);
}
