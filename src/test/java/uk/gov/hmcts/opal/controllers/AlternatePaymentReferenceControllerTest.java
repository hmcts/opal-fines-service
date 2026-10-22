package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.service.AlternatePaymentReferenceService;

@ExtendWith(MockitoExtension.class)
class AlternatePaymentReferenceControllerTest {

    @Mock
    private AlternatePaymentReferenceService alternatePaymentReferenceService;

    @InjectMocks
    private AlternatePaymentReferenceController controller;

    @Test
    void getAlternatePaymentReferences_returnsServiceResult() {
        Long defendantAccountId = 123L;
        List<AlternatePaymentReferencesItem> expected = List.of(
            new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(1L)
                .defendantAccountId(defendantAccountId)
                .aprText("REFERENCE-1"),
            new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(2L)
                .defendantAccountId(defendantAccountId)
                .aprText("REFERENCE-2")
        );
        when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId)).thenReturn(expected);

        ResponseEntity<List<AlternatePaymentReferencesItem>> response =
            controller.getAlternatePaymentReferences(defendantAccountId);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(expected, response.getBody()),
            () -> verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId)
        );
    }

    @Test
    void getAlternatePaymentReferences_returnsOkWithEmptyList() {
        Long defendantAccountId = 123L;
        when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId)).thenReturn(List.of());

        ResponseEntity<List<AlternatePaymentReferencesItem>> response =
            controller.getAlternatePaymentReferences(defendantAccountId);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(List.of(), response.getBody()),
            () -> verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId)
        );
    }

    @Test
    void getAlternatePaymentReferences_propagatesAccessDeniedException() {
        Long defendantAccountId = 123L;
        AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");
        when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId)).thenThrow(expected);

        AccessDeniedException actual = assertThrows(
            AccessDeniedException.class,
            () -> controller.getAlternatePaymentReferences(defendantAccountId));

        assertSame(expected, actual);
        verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId);
    }
}
