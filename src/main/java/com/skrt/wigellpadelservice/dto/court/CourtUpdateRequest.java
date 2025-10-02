package com.skrt.wigellpadelservice.dto.court;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CourtUpdateRequest(
        @NotNull UUID id,
        @NotBlank String name,
        @Min(1) int maxPlayers
){}
