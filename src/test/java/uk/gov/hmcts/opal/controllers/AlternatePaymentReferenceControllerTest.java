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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesWriteRequest;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesWriteRequest;
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

    @Nested
    @DisplayName("Create an alternate payment reference for a defendant account")
    class CreateAlternatePaymentReference {

        @ParameterizedTest
        @ValueSource(longs = {0L, 7L})
        @DisplayName("Returns the created reference with a Created status and its version as a quoted ETag")
        void returnsCreatedReferenceWithCreatedStatusAndQuotedVersionEtag(long version) {
            Long defendantAccountId = 123L;
            String expectedVersion = "\"" + version + "\"";
            AlternatePaymentReferencesWriteRequest request = new AlternatePaymentReferencesWriteRequest()
                .aprText("REFERENCE-1");
            AlternatePaymentReferencesItem expected = new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(1L)
                .defendantAccountId(defendantAccountId)
                .aprText("REFERENCE-1")
                .version(version);
            when(alternatePaymentReferenceService.createAlternatePaymentReferences(
                defendantAccountId, request, expectedVersion)).thenReturn(expected);

            ResponseEntity<AlternatePaymentReferencesItem> response =
                controller.createAlternatePaymentReference(defendantAccountId, expectedVersion, request);

            assertAll(
                () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                () -> assertSame(expected, response.getBody()),
                () -> assertEquals("\"" + version + "\"", response.getHeaders().getETag()),
                () -> verify(alternatePaymentReferenceService).createAlternatePaymentReferences(
                    defendantAccountId, request, expectedVersion)
            );
        }

        @Test
        @DisplayName("Propagates access denied when the service rejects the user's permissions")
        void propagatesAccessDeniedExceptionWhenServiceRejectsUserPermissions() {
            Long defendantAccountId = 123L;
            String expectedVersion = "\"3\"";
            AlternatePaymentReferencesWriteRequest request = new AlternatePaymentReferencesWriteRequest()
                .aprText("REFERENCE-1");
            AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");
            when(alternatePaymentReferenceService.createAlternatePaymentReferences(
                defendantAccountId, request, expectedVersion)).thenThrow(expected);

            AccessDeniedException actual = assertThrows(
                AccessDeniedException.class,
                () -> controller.createAlternatePaymentReference(defendantAccountId, expectedVersion, request));

            assertSame(expected, actual);
            verify(alternatePaymentReferenceService).createAlternatePaymentReferences(
                defendantAccountId, request, expectedVersion);
        }
    }

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
