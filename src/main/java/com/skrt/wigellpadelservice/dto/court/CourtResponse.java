package com.skrt.wigellpadelservice.dto.court;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourtResponse(
        String id,
        String name,
        int maxPlayers,
        boolean active,
        LocalDateTime deactivatedAt
){}
