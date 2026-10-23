package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("Alternate payment reference controller")
class AlternatePaymentReferenceControllerTest {

    @Mock
    private AlternatePaymentReferenceService alternatePaymentReferenceService;

    @InjectMocks
    private AlternatePaymentReferenceController controller;

    @Nested
    @DisplayName("Get alternate payment references for a defendant account")
    class GetAlternatePaymentReferences {

        @Test
        @DisplayName("Returns the service's payment references with an OK status")
        void returnsServiceReferencesWithOkStatus() {
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
            when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId))
                .thenReturn(expected);

            ResponseEntity<List<AlternatePaymentReferencesItem>> response =
                controller.getAlternatePaymentReferences(defendantAccountId);

            assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(expected, response.getBody()),
                () -> verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId)
            );
        }

        @Test
        @DisplayName("Returns an empty list with an OK status when no payment references exist")
        void returnsEmptyListWithOkStatusWhenNoReferencesExist() {
            Long defendantAccountId = 123L;
            when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId))
                .thenReturn(List.of());

            ResponseEntity<List<AlternatePaymentReferencesItem>> response =
                controller.getAlternatePaymentReferences(defendantAccountId);

            assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(List.of(), response.getBody()),
                () -> verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId)
            );
        }

        @Test
        @DisplayName("Propagates access denied when the service rejects the user's permissions")
        void propagatesAccessDeniedExceptionWhenServiceRejectsUserPermissions() {
            Long defendantAccountId = 123L;
            AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");
            when(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId))
                .thenThrow(expected);

            AccessDeniedException actual = assertThrows(
                AccessDeniedException.class,
                () -> controller.getAlternatePaymentReferences(defendantAccountId));

            assertSame(expected, actual);
            verify(alternatePaymentReferenceService).getAlternatePaymentReferences(defendantAccountId);
        }
    }
}
