package com.skrt.wigellpadelservice.services.impl;

import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.exceptions.BadRequestException;
import com.skrt.wigellpadelservice.exceptions.ResourceNotFoundException;
import com.skrt.wigellpadelservice.repositories.PadelCourtRepository;
import com.skrt.wigellpadelservice.services.PadelCourtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.skrt.wigellpadelservice.utility.SecurityUtil.*;

@Service
@Transactional
public class PadelCourtServiceImpl implements PadelCourtService {

    private static final Logger logger = LoggerFactory.getLogger(PadelCourtServiceImpl.class);

    private final PadelCourtRepository courtRepository;

    @Autowired
    public PadelCourtServiceImpl(PadelCourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<PadelCourt> listCourts() {
        return isAdmin()
                ? courtRepository.findAllByOrderByNameAsc()
                : courtRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    public PadelCourt addCourt(PadelCourt court) {
        validateAndNormalize(court);

        if(courtRepository.existsByNameIgnoreCase(court.getName())) {
            throw new BadRequestException("name", "already exists", court.getName());
        }

        PadelCourt saved = courtRepository.save(court);

        logger.info("court created by '{}': id={}, name='{}', maxPlayers={}",
                currentUsername(), saved.getId(), saved.getName(), saved.getMaxPlayers());

        return saved;
    }

    @Override
    public PadelCourt updateCourt(PadelCourt court) {
        if(court.getId() == null) {
            throw new BadRequestException("id", "is required");
        }
        validateAndNormalize(court);

        PadelCourt existingCourt = courtRepository.findByIdAndActiveTrue(court.getId())
                .orElseThrow(() -> new ResourceNotFoundException("court", court.getId()));

        courtRepository.findByNameIgnoreCase(court.getName())
                .filter(other -> !other.getId().equals(court.getId()))
                .ifPresent(other -> {
                    throw new BadRequestException("name", "already exists", court.getName());
                });

        existingCourt.setName(court.getName());
        existingCourt.setMaxPlayers(court.getMaxPlayers());

        PadelCourt saved = courtRepository.save(existingCourt);
        logger.info("court updated by '{}': id={}, name='{}', maxPlayers={}",
               currentUsername(), saved.getId(), saved.getName(), saved.getMaxPlayers());
        return saved;

    }

    @Override
    public void removeCourt(UUID courtId) {
        PadelCourt existing = courtRepository.findByIdAndActiveTrue(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("court", courtId));
        existing.setActive(false);
        existing.setDeactivatedAt(LocalDateTime.now());
        courtRepository.save(existing);

        logger.info("court removed by '{}': id={}, name='{}'",
                currentUsername(), existing.getId(), existing.getName());

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PadelCourt> getCourtById(UUID courtId) {
        return  isAdmin()
                ? courtRepository.findById(courtId)
                : courtRepository.findByIdAndActiveTrue(courtId);
    }

    private static void validateAndNormalize(PadelCourt court) {
        if(court == null) {
            throw new BadRequestException("court", "is required");
        }

        String name = court.getName();
        if(name == null || name.isBlank()) {
            throw new BadRequestException("name", "is required");
        }

        name = name.trim();

        if(name.length() > 100){
            throw new BadRequestException("name", "too long", name.length());
        }

        court.setName(name);

        int max = court.getMaxPlayers();
        if(max <= 0) {
            throw new BadRequestException("maxPlayers", "must be greater than 0", max);
        }
    }

}
