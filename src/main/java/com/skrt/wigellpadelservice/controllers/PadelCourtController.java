package com.skrt.wigellpadelservice.controllers;

import com.skrt.wigellpadelservice.dto.court.CourtCreateRequest;
import com.skrt.wigellpadelservice.dto.court.CourtResponse;
import com.skrt.wigellpadelservice.dto.court.CourtUpdateRequest;
import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.mappers.CourtMapper;
import com.skrt.wigellpadelservice.services.PadelCourtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/wigellpadel")
@Validated
public class PadelCourtController {

    private final PadelCourtService courtService;
    private final CourtMapper courtMapper;

    @Autowired
    public PadelCourtController(PadelCourtService courtService, CourtMapper courtMapper) {
        this.courtService = courtService;
        this.courtMapper = courtMapper;
    }

    @GetMapping("/listcourts")
    public ResponseEntity<List<CourtResponse>> listCourts() {
        List<PadelCourt> courts = courtService.listCourts();
        List<CourtResponse> response = courts
                .stream()
                .map(courtMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/addcourt")
    public ResponseEntity<CourtResponse> addCourt(@Valid @RequestBody CourtCreateRequest request) {
        PadelCourt toSave = courtMapper.fromCreate(request);
        PadelCourt saved = courtService.addCourt(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(courtMapper.toResponse(saved));
    }

    @PutMapping("/v1/updatecourt")
    public ResponseEntity<CourtResponse> updateCourt(@Valid @RequestBody CourtUpdateRequest request) {
        PadelCourt toUpdate = courtMapper.fromUpdate(request);
        PadelCourt saved = courtService.updateCourt(toUpdate);
        return ResponseEntity.ok(courtMapper.toResponse(saved));
    }

    @DeleteMapping("/v1/remcourt/{id}")
    public ResponseEntity<Void> removeCourt(@PathVariable("id") UUID courtId){
        courtService.removeCourt(courtId);
        return ResponseEntity.noContent().build();
    }

}
