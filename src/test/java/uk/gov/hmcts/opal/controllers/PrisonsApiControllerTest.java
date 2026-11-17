package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;
import uk.gov.hmcts.opal.generated.model.PrisonsReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.PrisonService;

@ExtendWith(MockitoExtension.class)
class PrisonsApiControllerTest {

    @Mock
    private PrisonService prisonService;

    @InjectMocks
    private PrisonsApiController controller;

    @Test
    void shouldGetPrisonByIdFromService() {
        // Given
        Long prisonId = 123L;
        PrisonsPrison prison = PrisonsPrison.builder().prisonId(prisonId).name("Test Prison").build();
        when(prisonService.getPrisonById(prisonId)).thenReturn(prison);

        // When
        ResponseEntity<PrisonsPrison> response = controller.getPrisonById(prisonId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(prison);
        verify(prisonService).getPrisonById(prisonId);
    }

    @Test
    void shouldGetPrisonReferenceDataFromService() {
        // Given
        PrisonsReferenceDataResponse referenceData = PrisonsReferenceDataResponse.builder()
            .count(1)
            .prisons(List.of(PrisonsPrisonSummary.builder().prisonId(123L).name("Test Prison").build()))
            .build();
        when(prisonService.getPrisonsRefData()).thenReturn(referenceData);

        // When
        ResponseEntity<PrisonsReferenceDataResponse> response = controller.getPrisonsRefData();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(referenceData);
        verify(prisonService).getPrisonsRefData();
    }
}
