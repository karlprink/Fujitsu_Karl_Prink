package com.fujitsu.delivery.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unit tests for GlobalExceptionHandler.
 * Uses a standalone MockMvc setup with a dummy controller to verify that
 * exceptions are intercepted and correctly formatted as JSON error responses.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Verifies that VehicleForbiddenException results in a 400 Bad Request JSON response.
     */
    @Test
    void handleVehicleForbiddenException_returns400AndJson() throws Exception {
        mockMvc.perform(get("/dummy/forbidden"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Vehicle is forbidden"));
    }

    /**
     * Verifies that IllegalArgumentException results in a 400 Bad Request JSON response.
     */
    @Test
    void handleIllegalArgumentException_returns400AndJson() throws Exception {
        mockMvc.perform(get("/dummy/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.message").value("Invalid argument passed"));
    }

    /**
     * Verifies that missing request parameters result in a 400 Bad Request JSON response.
     */
    @Test
    void handleMissingParamsException_returns400AndJson() throws Exception {
        mockMvc.perform(get("/dummy/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.message").value("Missing required parameter: city"));
    }

    /**
     * Verifies that unhandled general exceptions result in a 500 Internal Server Error JSON response.
     */
    @Test
    void handleGeneralException_returns500AndJson() throws Exception {
        mockMvc.perform(get("/dummy/general-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred"));
    }

    /**
     * Verifies that malformed JSON requests result in a 400 Bad Request JSON response
     * containing the expected format guide.
     */
    @Test
    void handleMalformedJsonException_returns400AndCustomFormat() throws Exception {
        mockMvc.perform(post("/dummy/malformed-json"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.expectedFormat.city").value("String (e.g., 'TALLINN')"));
    }

    /**
     * Dummy REST controller used only for triggering exceptions in tests.
     */
    @RestController
    private static class DummyController {
        @GetMapping("/dummy/forbidden")
        public void throwForbidden() { throw new VehicleForbiddenException("Vehicle is forbidden"); }

        @GetMapping("/dummy/illegal-arg")
        public void throwIllegalArg() { throw new IllegalArgumentException("Invalid argument passed"); }

        @GetMapping("/dummy/missing-param")
        public void throwMissingParam() throws MissingServletRequestParameterException {
            throw new MissingServletRequestParameterException("city", "String");
        }

        @GetMapping("/dummy/general-error")
        public void throwGeneralError() throws Exception { throw new Exception("Unexpected error"); }

        @PostMapping("/dummy/malformed-json")
        public void throwMalformedJson() {
            throw new HttpMessageNotReadableException("JSON parse error", (org.springframework.http.HttpInputMessage) null);
        }
    }
}