package com.skrt.wigellpadelservice.mappers;

import com.skrt.wigellpadelservice.dto.court.CourtCreateRequest;
import com.skrt.wigellpadelservice.dto.court.CourtResponse;
import com.skrt.wigellpadelservice.dto.court.CourtUpdateRequest;
import com.skrt.wigellpadelservice.entities.PadelCourt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourtMapper {

    public CourtResponse toResponse(PadelCourt court) {
        return new CourtResponse(
                toString(court.getId()),
                court.getName(),
                court.getMaxPlayers(),
                court.isActive(),
                court.getDeactivatedAt()
        );
    }

    public PadelCourt fromCreate(CourtCreateRequest request) {
        PadelCourt court = new PadelCourt();
        court.setName(request.name());
        court.setMaxPlayers(request.maxPlayers());
        return court;
    }

    public PadelCourt fromUpdate(CourtUpdateRequest request) {
        PadelCourt court = new PadelCourt();
        court.setId(request.id());
        court.setName(request.name());
        court.setMaxPlayers(request.maxPlayers());
        return court;
    }

    private static String toString(UUID id){
        return id != null ? id.toString() : null;
    }
}
