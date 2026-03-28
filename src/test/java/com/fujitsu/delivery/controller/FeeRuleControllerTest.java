package com.fujitsu.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC tests for the FeeRuleController.
 * Verifies the REST API endpoints for managing BaseFee rules (CRUD operations).
 */
@WebMvcTest(FeeRuleController.class)
class FeeRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaseFeeRepository baseFeeRepository;

    /**
     * Tests fetching all base fees.
     * Expects a 200 OK status and a JSON array containing the rules.
     */
    @Test
    void getAllBaseFees_returnsListOfFees() throws Exception {
        BaseFee fee1 = BaseFee.builder().id(1L).city("TALLINN").vehicleType("BIKE").fee(3.0).build();
        BaseFee fee2 = BaseFee.builder().id(2L).city("TARTU").vehicleType("CAR").fee(3.5).build();

        when(baseFeeRepository.findAll()).thenReturn(List.of(fee1, fee2));

        mockMvc.perform(get("/api/rules/base-fees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].city").value("TALLINN"))
                .andExpect(jsonPath("$[1].city").value("TARTU"));
    }

    /**
     * Tests creating a new base fee.
     * Expects a 200 OK status and the saved JSON object.
     */
    @Test
    void createBaseFee_validRequest_returnsSavedFee() throws Exception {
        BaseFee newFee = BaseFee.builder().city("NARVA").vehicleType("SCOOTER").fee(2.0).build();
        BaseFee savedFee = BaseFee.builder().id(3L).city("NARVA").vehicleType("SCOOTER").fee(2.0).build();

        when(baseFeeRepository.save(any(BaseFee.class))).thenReturn(savedFee);

        mockMvc.perform(post("/api/rules/base-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.city").value("NARVA"));
    }

    /**
     * Tests updating an existing base fee successfully.
     * Expects a 200 OK status and the updated JSON object.
     */
    @Test
    void updateBaseFee_existingId_returnsUpdatedFee() throws Exception {
        Long id = 1L;
        BaseFee existingFee = BaseFee.builder().id(id).city("TALLINN").vehicleType("BIKE").fee(3.0).build();
        BaseFee updateRequest = BaseFee.builder().city("TALLINN").vehicleType("BIKE").fee(4.0).build();

        when(baseFeeRepository.findById(id)).thenReturn(Optional.of(existingFee));
        when(baseFeeRepository.save(any(BaseFee.class))).thenReturn(existingFee);

        mockMvc.perform(put("/api/rules/base-fees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee").value(4.0));
    }

    /**
     * Tests updating a base fee that does not exist.
     * Expects a 404 Not Found status.
     */
    @Test
    void updateBaseFee_nonExistingId_returnsNotFound() throws Exception {
        Long id = 99L;
        BaseFee updateRequest = BaseFee.builder().city("TALLINN").vehicleType("BIKE").fee(4.0).build();

        when(baseFeeRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/rules/base-fees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests deleting an existing base fee.
     * Expects a 200 OK status and verifies the repository delete method was called.
     */
    @Test
    void deleteBaseFee_existingId_returnsOk() throws Exception {
        Long id = 1L;
        when(baseFeeRepository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/rules/base-fees/{id}", id))
                .andExpect(status().isOk());

        verify(baseFeeRepository, times(1)).deleteById(id);
    }

    /**
     * Tests deleting a base fee that does not exist.
     * Expects a 404 Not Found status and verifies the repository delete method was never called.
     */
    @Test
    void deleteBaseFee_nonExistingId_returnsNotFound() throws Exception {
        Long id = 99L;
        when(baseFeeRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/rules/base-fees/{id}", id))
                .andExpect(status().isNotFound());

        verify(baseFeeRepository, never()).deleteById(anyLong());
    }
}