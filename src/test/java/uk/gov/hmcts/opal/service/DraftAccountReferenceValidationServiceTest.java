package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;

@ExtendWith(MockitoExtension.class)
class DraftAccountReferenceValidationServiceTest {

    @Mock
    private CourtLiteRepository courtLiteRepository;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private MajorCreditorRepository majorCreditorRepository;

    @InjectMocks
    private DraftAccountReferenceValidationService service;

    @Test
    void validateReferences_whenAllReferencesExist_shouldPass() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq((short) 77))).thenReturn(true);
        when(resultRepository.existsById(anyString())).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);

        assertDoesNotThrow(() -> service.validateReferences(validAccountJson(), (short) 77));
    }

    @Test
    void validateReferences_whenOffenceDoesNotExist_shouldReportOffencePath() {
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(999L, (short) 77))
            .thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences("""
            {
              "offences": [
                {
                  "offence_id": 999
                }
              ]
            }
            """, (short) 77)
        );

        assertContains(
            exception.getMessage(),
            "account.offences[0].offence_id: offence id 999 does not exist"
        );

        verify(offenceRepository)
            .existsByOffenceIdAvailableToBusinessUnit(999L, (short) 77);
    }

    @Test
    void validateReferences_whenMultipleOffencesAreInvalid_shouldReportEachOffencePath() {
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(999L, (short) 77))
            .thenReturn(false);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(998L, (short) 77))
            .thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences("""
            {
              "offences": [
                {
                  "offence_id": 999
                },
                {
                  "offence_id": 998
                }
              ]
            }
            """, (short) 77)
        );

        String message = exception.getMessage();

        assertContains(message, "Draft account reference validation failed with 2 error(s):");
        assertContains(message, "account.offences[0].offence_id: offence id 999 does not exist");
        assertContains(message, "account.offences[1].offence_id: offence id 998 does not exist");

        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(999L, (short) 77);
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(998L, (short) 77);
    }

    @Test
    void validateReferences_whenSomeReferencesAreMissing_shouldReportAllFailures() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(false);
        when(resultRepository.findById("FO")).thenReturn(Optional.empty());
        when(resultRepository.findById("FVS")).thenReturn(Optional.empty());
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq((short) 77))).thenReturn(false);
        when(resultRepository.existsById(anyString())).thenReturn(false);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), (short) 77));

        String message = exception.getMessage();
        assertContains(message, "$.enforcement_court_id");
        assertContains(message, "account.offences[0].offence_id");
        assertContains(message, "$.offences[0].imposing_court_id");
        assertContains(message, "$.offences[0].impositions[0].result_id");
        assertContains(message, "$.offences[0].impositions[0].major_creditor_id");
        assertContains(message, "account.offences[1].offence_id");
        assertContains(message, "$.offences[1].imposing_court_id");
        assertContains(message, "$.offences[1].impositions[0].result_id");
        assertContains(message, "$.offences[1].impositions[0].major_creditor_id");
        assertContains(message, "$.payment_terms.enforcements[0].result_id");
        assertContains(message, "$.payment_terms.enforcements[1].result_id");

        verify(courtLiteRepository, times(3)).existsById(anyLong());
        verify(resultRepository, times(2)).findById(anyString());
        verify(resultRepository, times(2)).existsById(anyString());
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(21L, (short) 77);
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(22L, (short) 77);
        verify(majorCreditorRepository, times(2)).existsById(anyLong());
    }

    @Test
    void validateReferences_whenImpositionResultIsNotAnImposition_shouldReportFailure() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeNonImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.existsById("COLLO")).thenReturn(true);
        when(resultRepository.existsById("MISSING")).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), (short) 77));

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
    }

    @Test
    void validateReferences_whenImpositionResultIsInactive_shouldReportFailure() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(inactiveImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.existsById("COLLO")).thenReturn(true);
        when(resultRepository.existsById("MISSING")).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), (short) 77));

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
    }

    private static void assertContains(String message, String fragment) {
        org.junit.jupiter.api.Assertions.assertTrue(message.contains(fragment),
            () -> "Expected message to contain: " + fragment + "\nActual message:\n" + message);
    }

    private static String validAccountJson() {
        return """
            {
              "enforcement_court_id": 11,
              "offences": [
                {
                  "offence_id": 21,
                  "imposing_court_id": 31,
                  "impositions": [
                    {
                      "result_id": "FO",
                      "major_creditor_id": 41
                    }
                  ]
                },
                {
                  "offence_id": 22,
                  "imposing_court_id": 32,
                  "impositions": [
                    {
                      "result_id": "FVS",
                      "major_creditor_id": 42
                    }
                  ]
                }
              ],
              "payment_terms": {
                "payment_terms_type_code": "B",
                "enforcements": [
                  {
                    "result_id": "COLLO"
                  },
                  {
                    "result_id": "MISSING"
                  }
                ]
              }
            }
            """;
    }

    private static ResultEntity activeImpositionResult(String resultId) {
        return ResultEntity.builder()
            .resultId(resultId)
            .active(true)
            .imposition(true)
            .build();
    }

    private static ResultEntity activeNonImpositionResult(String resultId) {
        return ResultEntity.builder()
            .resultId(resultId)
            .active(true)
            .imposition(false)
            .build();
    }

    private static ResultEntity inactiveImpositionResult(String resultId) {
        return ResultEntity.builder()
            .resultId(resultId)
            .active(false)
            .imposition(true)
            .build();
    }
}
