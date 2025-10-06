package com.skrt.wigellpadelservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skrt.wigellpadelservice.entities.PadelBooking;
import com.skrt.wigellpadelservice.entities.PadelCourt;
import com.skrt.wigellpadelservice.entities.PadelCustomer;
import com.skrt.wigellpadelservice.repositories.PadelBookingRepository;
import com.skrt.wigellpadelservice.repositories.PadelCourtRepository;
import com.skrt.wigellpadelservice.repositories.PadelCustomerRepository;
import com.skrt.wigellpadelservice.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(PadelBookingControllerTest.TestCurrencyConfig.class)
class PadelBookingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PadelCourtRepository courtRepository;

    @Autowired
    PadelCustomerRepository customerRepository;

    @Autowired
    PadelBookingRepository bookingRepository;

    @TestConfiguration
    static class TestCurrencyConfig{
        @Bean
        @Primary
        public CurrencyService currencyServiceStub(){
            return amountSek -> new java.math.BigDecimal("10.00");
        }
    }

    @BeforeEach
    void resetDb(){
        bookingRepository.deleteAll();
        courtRepository.deleteAll();
        customerRepository.deleteAll();
    }


    private UUID seedCourt(String name, int maxPlayers, boolean active){
        PadelCourt court = new PadelCourt();
        court.setName(name);
        court.setMaxPlayers(maxPlayers);
        court.setActive(active);
        return courtRepository.save(court).getId();
    }

    private PadelCustomer ensureCustomer(String name){
        return customerRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    PadelCustomer customer = new PadelCustomer();
                    customer.setName(name);
                    return customerRepository.save(customer);
                });
    }


    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void checkAvailability_slotFree() throws Exception {
        UUID courtId = seedCourt("Center Court", 4, true);

        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, "2099-10-14")
                       .accept(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.courtId").value(courtId.toString()))
                 .andExpect(jsonPath("$.courtName").value("Center Court"))
                 .andExpect(jsonPath("$.maxPlayers").value(4))
                 .andExpect(jsonPath("$.availableSlots").isArray());
    }

    @Test
    @WithMockUser(username = "alex", roles ={"USER"})
    void checkAvailability_excludesBookedSlot() throws Exception {
        UUID courtId = seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                 {
                  "courtName":"Center Court","date":"2099-10-14","time":"10:00:00","players": 2
                 }
             """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/wigellpadel/checkavailability/{courtId}/{date}", courtId, "2099-10-14")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots", not(hasItem("10:00:00"))));
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_createsBooking() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {
                  "courtName":"Center Court","date":"2099-10-14","time":"10:00:00","players": 3
                  }
              """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.courtName").value("Center Court"))
                .andExpect(jsonPath("$.players").value(3));
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_unknownCourt() throws Exception {
        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "courtName": "Center Court", "date": "2099-10-14","time":"10:00:00","players": 3
                    }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_tooManyPlayers() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "courtName": "Center Court", "date": "2099-10-14","time":"10:00:00","players": 5
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_slotAlreadyTaken() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                     "courtName": "Center Court", "date": "2099-10-14","time":"10:00:00","players": 3
                    }
                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                     "courtName": "Center Court", "date": "2099-10-14","time":"10:00:00","players": 3
                    }
                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_blankCourtName() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                 "courtName": "   ", "date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_nullCourtName() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                 "courtName": null, "date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void bookCourt_missingCourtName() throws Exception {
        seedCourt("Center Court", 4, true);
        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                 "date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void myBookings_currentUserBookings() throws Exception {
        seedCourt("Center Court", 4, true);

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                 "courtName": "Center Court","date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/wigellpadel/v1/mybookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courtName").value("Center Court"));
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void updateBooking_ownerOk() throws Exception {
        seedCourt("Center Court", 4, true);

        String created = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                "courtName": "Center Court","date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                        "id":"%s","date":"2099-10-14","time":"11:00:00","players": 3
                        }
                        """).formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("11:00:00"));
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void updateBooking_wrongId() throws Exception {
        String wrongId = java.util.UUID.randomUUID().toString();

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                        "id":"%s","date":"2099-10-14","time":"11:00:00","players": 3
                        }
                        """).formatted(wrongId)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "sara", roles = {"USER"})
    void updateBooking_wrongUser() throws Exception{
        UUID courtId = seedCourt("Center Court", 4, true);
        PadelCustomer alex = ensureCustomer("alex");

        PadelBooking booking = new PadelBooking();
        booking.setCustomer(alex);
        booking.setCourt(courtRepository.findById(courtId).orElseThrow());
        booking.setDate(java.time.LocalDate.of(2099,12,1));
        booking.setTime(java.time.LocalTime.of(10,0));
        booking.setPlayers(2);
        booking.setTotalPriceSek(new java.math.BigDecimal("300"));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                        "id":"%s","date":"2099-12-01","time":"11:00:00","players": 3
                        }
                        """).formatted(booking.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void updateBooking_exceedsMaxPlayers() throws Exception{
        seedCourt("Center Court", 4, true);

        String created = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "courtName":"Center Court","date":"2099-10-14","time":"10:00:00","players":3
                        }
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                        "id":"%s","date":"2099-12-01","time":"11:00:00","players": 5
                        }
                        """).formatted(id)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void updateBooking_sameSlotPlayerChange() throws Exception{
        seedCourt("Center Court", 4, true);

        String created = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "courtName":"Center Court","date":"2099-10-14","time":"10:00:00","players":3
                        }
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                        "id":"%s","date":"2099-12-01","time":"10:00:00","players": 2
                        }
                        """).formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").value(2))
                .andExpect(jsonPath("$.time").value("10:00:00"));
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void updateBooking_takenSlot() throws Exception{
        seedCourt("Center Court", 4, true);

        String firstBooking = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "courtName":"Center Court","date":"2099-10-14","time":"10:00:00","players":2
                        }
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstBookingId = objectMapper.readTree(firstBooking).get("id").asText();

        mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "courtName":"Center Court","date":"2099-10-14","time":"11:00:00","players":2
                        }
                    """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/wigellpadel/v1/updatebooking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                        {
                         "id":"%s","date":"2099-10-14","time":"11:00:00","players":2
                        }
                    """).formatted(firstBookingId)))
                .andExpect(status().isConflict());
    }


    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void cancelBooking_beforeDeadline() throws Exception{
        seedCourt("Center Court", 4, true);

        String created = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                "courtName": "Center Court","date": "2099-10-14","time":"10:00:00","players": 3
                }
                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(delete("/api/wigellpadel/v1/cancelbooking").param("id",id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "alex", roles = {"USER"})
    void cancelBooking_afterDeadline() throws Exception {
        seedCourt("Center Court", 4, true);

        String soon = LocalDate.now().plusDays(3).toString();
        String created = mockMvc.perform(post("/api/wigellpadel/v1/booking/bookcourt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(("""
                {
                "courtName": "Center Court","date": "%s","time":"10:00:00","players": 3
                }
                """).formatted(soon)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(delete("/api/wigellpadel/v1/cancelbooking").param("id",id))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "sara", roles = {"USER"})
    void cancelBooking_otherUser() throws Exception{
        UUID courtId = seedCourt("Center Court", 4, true);
        PadelCustomer alex = ensureCustomer("alex");

        PadelBooking booking = new PadelBooking();
        booking.setCustomer(alex);
        booking.setCourt(courtRepository.findById(courtId).orElseThrow());
        booking.setDate(java.time.LocalDate.of(2099,12,1));
        booking.setTime(java.time.LocalTime.of(10,0));
        booking.setPlayers(2);
        booking.setTotalPriceSek(new java.math.BigDecimal("300"));
        booking = bookingRepository.save(booking);

        mockMvc.perform(delete("/api/wigellpadel/v1/cancelbooking").param("id", booking.getId().toString()))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listCanceled_adminOnlyCanceled() throws Exception {
        UUID courtId = seedCourt("Center Court", 4, true);
        PadelCustomer alex = ensureCustomer("alex");

        PadelBooking booking = new PadelBooking();
        booking.setCustomer(alex);
        booking.setCourt(courtRepository.findById(courtId).orElseThrow());
        booking.setDate(java.time.LocalDate.now().plusDays(10));
        booking.setTime(java.time.LocalTime.of(10,0));
        booking.setPlayers(2);
        booking.setTotalPriceSek(new java.math.BigDecimal("300"));
        booking.setCanceled(true);
        booking = bookingRepository.save(booking);

        mockMvc.perform(get("/api/wigellpadel/v1/listcanceled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].canceled", everyItem(is(true))));

    }

    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listUpcoming_excludesCanceled() throws Exception {
        UUID courtId = seedCourt("Center Court", 4, true);
        PadelCustomer alex = ensureCustomer("alex");

        PadelBooking upcomingBooking = new PadelBooking();
        upcomingBooking.setCustomer(alex);
        upcomingBooking.setCourt(courtRepository.findById(courtId).orElseThrow());
        upcomingBooking.setDate(java.time.LocalDate.now().plusDays(10));
        upcomingBooking.setTime(java.time.LocalTime.of(10,0));
        upcomingBooking.setPlayers(2);
        upcomingBooking.setTotalPriceSek(new java.math.BigDecimal("300"));
        bookingRepository.save(upcomingBooking);

        PadelBooking cancelledBooking = new PadelBooking();
        cancelledBooking.setCustomer(alex);
        cancelledBooking.setCourt(courtRepository.findById(courtId).orElseThrow());
        cancelledBooking.setDate(java.time.LocalDate.now().plusDays(20));
        cancelledBooking.setTime(java.time.LocalTime.of(10,0));
        cancelledBooking.setPlayers(2);
        cancelledBooking.setTotalPriceSek(new java.math.BigDecimal("300"));
        cancelledBooking.setCanceled(true);
        bookingRepository.save(cancelledBooking);

        mockMvc.perform(get("/api/wigellpadel/v1/listupcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].canceled", everyItem(is(false))));
    }

    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listUpcoming_usesTodayNow() throws Exception {
        mockMvc.perform(get("/api/wigellpadel/v1/listupcoming"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listUpcoming_withToday_usesPrividedDate() throws Exception {
        mockMvc.perform(get("/api/wigellpadel/v1/listupcoming").param("today", "2099-01-01"))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listPast_beforeToday() throws Exception {
        UUID courtId = seedCourt("Center Court", 4, true);
        PadelCustomer alex = ensureCustomer("alex");

        PadelBooking booking = new PadelBooking();
        booking.setCustomer(alex);
        booking.setCourt(courtRepository.findById(courtId).orElseThrow());
        booking.setDate(java.time.LocalDate.of(2000,1,1));
        booking.setTime(java.time.LocalTime.of(10,0));
        booking.setPlayers(3);
        booking.setTotalPriceSek(new java.math.BigDecimal("300"));
        booking = bookingRepository.save(booking);

        mockMvc.perform(get("/api/wigellpadel/v1/listpast").param("today", "2000-02-01"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "simon", roles = {"ADMIN"})
    void listPast_usesTodayNow()  throws Exception {
        mockMvc.perform(get("/api/wigellpadel/v1/listpast"))
                .andExpect(status().isOk());
    }

}