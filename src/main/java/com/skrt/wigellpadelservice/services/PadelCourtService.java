package com.skrt.wigellpadelservice.services;

import com.skrt.wigellpadelservice.entities.PadelCourt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PadelCourtService {

    List<PadelCourt> listCourts();

    PadelCourt addCourt (PadelCourt court);

    PadelCourt updateCourt (PadelCourt court);

    void removeCourt (UUID courtId);

    Optional<PadelCourt> getCourtById (UUID courtId);
}
