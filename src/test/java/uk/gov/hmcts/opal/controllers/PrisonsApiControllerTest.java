package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;
import uk.gov.hmcts.opal.generated.model.PrisonsReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.PrisonService;

@ExtendWith(MockitoExtension.class)
class PrisonsApiControllerTest {
    @Mock
    private PrisonService prisonService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new PrisonsApiController(prisonService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void getPrisonByIdReturnsPrison() throws Exception {
        // Given
        when(prisonService.getPrisonById(123L)).thenReturn(
            PrisonsPrison.builder().prisonId(123L).name("Test Prison").build());

        // When / Then
        mockMvc.perform(get("/prisons/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prison_id").value(123))
            .andExpect(jsonPath("$.name").value("Test Prison"));

        verify(prisonService).getPrisonById(123L);
    }

    @Test
    void getUnknownPrisonReturnsNotFound() throws Exception {
        // Given
        when(prisonService.getPrisonById(999L))
            .thenThrow(new EntityNotFoundException("Prison not found with id: 999"));

        // When / Then
        mockMvc.perform(get("/prisons/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPrisonReferenceDataReturnsPrisons() throws Exception {
        // Given
        when(prisonService.getPrisonsRefData()).thenReturn(PrisonsReferenceDataResponse.builder()
            .count(1)
            .prisons(List.of(PrisonsPrisonSummary.builder().prisonId(123L).name("Test Prison").build()))
            .build());

        // When / Then
        mockMvc.perform(get("/prisons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.prisons[0].prison_id").value(123))
            .andExpect(jsonPath("$.prisons[0].name").value("Test Prison"));

        verify(prisonService).getPrisonsRefData();
    }
}
