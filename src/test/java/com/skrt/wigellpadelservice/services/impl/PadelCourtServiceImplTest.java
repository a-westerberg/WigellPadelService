package com.skrt.wigellpadelservice.services.impl;

import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.exceptions.BadRequestException;
import com.skrt.wigellpadelservice.exceptions.ResourceNotFoundException;
import com.skrt.wigellpadelservice.repositories.PadelCourtRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PadelCourtServiceImplTest {

    @Mock
    PadelCourtRepository courtRepository;

    @InjectMocks
    PadelCourtServiceImpl courtService;

    AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alex", "alex",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.getContext();
        mocks.close();
    }

    private static void asAdmin(){
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "simon", "simon",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @Test
    void listCourts_userGetOnlyActive() {
        PadelCourt active = new PadelCourt();
        active.setId(UUID.randomUUID());
        active.setName("Court A");
        active.setMaxPlayers(4);
        active.setActive(true);

        when(courtRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(active));

        List<PadelCourt> result = courtService.listCourts();

        assertEquals(1, result.size());
        assertTrue(result.getFirst().isActive());
        verify(courtRepository).findByActiveTrueOrderByNameAsc();
        verify(courtRepository, never()).findAllByOrderByNameAsc();
    }

    @Test
    void listCourts_adminGetsAll(){
        asAdmin();

        PadelCourt inactive = new PadelCourt();
        inactive.setId(UUID.randomUUID());
        inactive.setName("Court B");
        inactive.setMaxPlayers(4);
        inactive.setActive(false);

        when(courtRepository.findAllByOrderByNameAsc()).thenReturn(List.of(inactive));

        List<PadelCourt> result = courtService.listCourts();

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isActive());
        verify(courtRepository).findAllByOrderByNameAsc();
        verify(courtRepository, never()).findByActiveTrueOrderByNameAsc();
    }

    @Test
    void addCourt_success() {
        PadelCourt input = new PadelCourt();
        input.setName("Court A");
        input.setMaxPlayers(4);

        when(courtRepository.existsByNameIgnoreCase("Court A")).thenReturn(false);
        when(courtRepository.save(any(PadelCourt.class))).thenAnswer(inv -> inv.getArgument(0));

        PadelCourt result = courtService.addCourt(input);

        assertEquals("Court A", result.getName());
        assertEquals(4, result.getMaxPlayers());
        verify(courtRepository).existsByNameIgnoreCase("Court A");
        verify(courtRepository).save(any(PadelCourt.class));
    }

    @Test
    void addCourt_rejectsDuplicateName(){
        PadelCourt input = new PadelCourt();
        input.setName("Court A");
        input.setMaxPlayers(4);

        when(courtRepository.existsByNameIgnoreCase("Court A")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(input));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    void addCourt_validatesNameRequiredIsBlank(){
        PadelCourt input = new PadelCourt();
        input.setName("   ");
        input.setMaxPlayers(4);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(input));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void addCourt_nullCourtThrows(){
        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(null));
        assertTrue(ex.getMessage().toLowerCase().contains("court"));
    }

    @Test
    void addCourt_nameNullThrows(){
        PadelCourt input = new PadelCourt();
        input.setName(null);
        input.setMaxPlayers(4);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(input));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void addCourt_nameTooLongThrows(){
        String tooLong = "nameToLong".repeat(100);
        PadelCourt input = new PadelCourt();
        input.setName(tooLong);
        input.setMaxPlayers(4);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(input));
        assertTrue(ex.getMessage().toLowerCase().contains("too long"));

    }

    @Test
    void addCourt_validatesMaxPlayersPositive(){
        PadelCourt input = new PadelCourt();
        input.setName("Court A");
        input.setMaxPlayers(0);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.addCourt(input));
        assertTrue(ex.getMessage().toLowerCase().contains("maxplayers"));
    }
    @Test
    void updateCourt_success() {
        UUID id = UUID.randomUUID();

        PadelCourt toUpdate = new PadelCourt();
        toUpdate.setId(id);
        toUpdate.setName("Court B");
        toUpdate.setMaxPlayers(3);

        PadelCourt existing = new PadelCourt();
        existing.setId(id);
        existing.setName("Court A");
        existing.setMaxPlayers(2);
        existing.setActive(true);

        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existing));
        when(courtRepository.findByNameIgnoreCase("Court B")).thenReturn(Optional.empty());
        when(courtRepository.save(any(PadelCourt.class))).thenAnswer(inv -> inv.getArgument(0));

        PadelCourt result = courtService.updateCourt(toUpdate);

        assertEquals("Court B", result.getName());
        assertEquals(3, result.getMaxPlayers());
        verify(courtRepository).findByIdAndActiveTrue(id);
        verify(courtRepository).findByNameIgnoreCase("Court B");
        verify(courtRepository).save(any(PadelCourt.class));
    }

    @Test
    void updateCourt_requiresId(){
        PadelCourt toUpdate = new PadelCourt();
        toUpdate.setName("Court B");
        toUpdate.setMaxPlayers(3);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.updateCourt(toUpdate));
        assertTrue(ex.getMessage().toLowerCase().contains("id"));
    }

    @Test
    void updateCourt_notFoundWhenInactiveOrMissing(){
        UUID id = UUID.randomUUID();

        PadelCourt toUpdate = new PadelCourt();
        toUpdate.setId(id);
        toUpdate.setName("Court A");
        toUpdate.setMaxPlayers(3);

        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courtService.updateCourt(toUpdate));
    }

    @Test
    void updateCourt_rejectsDuplicateName(){
        UUID id = UUID.randomUUID();

        PadelCourt toUpdate = new PadelCourt();
        toUpdate.setId(id);
        toUpdate.setName("Court A");
        toUpdate.setMaxPlayers(3);

        PadelCourt existing = new PadelCourt();
        existing.setId(id);
        existing.setName("Court B");
        existing.setMaxPlayers(2);
        existing.setActive(true);

        PadelCourt courtWithSameName = new PadelCourt();
        courtWithSameName.setId(UUID.randomUUID());
        courtWithSameName.setName("Court A");
        courtWithSameName.setMaxPlayers(3);

        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existing));
        when(courtRepository.findByNameIgnoreCase("Court A")).thenReturn(Optional.of(courtWithSameName));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> courtService.updateCourt(toUpdate));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));

    }


    @Test
    void removeCourt_softDelete() {
        UUID id = UUID.randomUUID();

        PadelCourt existing = new PadelCourt();
        existing.setId(id);
        existing.setName("Court A");
        existing.setMaxPlayers(3);
        existing.setActive(true);

        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existing));

        courtService.removeCourt(id);

        assertFalse(existing.isActive());
        assertNotNull(existing.getDeactivatedAt());
        verify(courtRepository).save(existing);
    }

    @Test
    void removeCourt_notFound(){
        UUID id = UUID.randomUUID();
        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courtService.removeCourt(id));
    }

    @Test
    void getCourtById_adminSeesAny() {
        asAdmin();
        UUID id = UUID.randomUUID();

        PadelCourt court = new PadelCourt();
        court.setId(id);
        court.setName("Court A");
        court.setMaxPlayers(3);
        court.setActive(true);

        when(courtRepository.findById(id)).thenReturn(Optional.of(court));

        Optional<PadelCourt> result = courtService.getCourtById(id);
        assertTrue(result.isPresent());
        verify(courtRepository).findById(id);
        verify(courtRepository, never()).findByIdAndActiveTrue(id);
    }

    @Test
    void getCourtById_userSeesOnlyActive() {
        UUID id = UUID.randomUUID();

        PadelCourt court = new PadelCourt();
        court.setId(id);
        court.setName("Court A");
        court.setMaxPlayers(3);
        court.setActive(true);

        when(courtRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(court));

        Optional<PadelCourt> result = courtService.getCourtById(id);
        assertTrue(result.isPresent());
        assertTrue(result.get().isActive());
        verify(courtRepository).findByIdAndActiveTrue(id);
        verify(courtRepository, never()).findById(id);
    }
}