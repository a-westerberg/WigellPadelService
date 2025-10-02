package com.skrt.wigellpadelservice.dto.court;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CourtCreateRequest(
        @NotBlank String name,
        @Min(1) int maxPlayers
) {}
