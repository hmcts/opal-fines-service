package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private static final short BUSINESS_UNIT_ID = 77;

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
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        assertDoesNotThrow(() -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));
    }

    @Test
    void validateReferences_whenOffenceDoesNotExist_shouldReportOffencePath() {
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(999L, BUSINESS_UNIT_ID)).thenReturn(false);

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
            """, BUSINESS_UNIT_ID)
        );

        assertContains(exception.getMessage(), "account.offences[0].offence_id: offence id 999 does not exist");
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(999L, BUSINESS_UNIT_ID);
    }

    @Test
    void validateReferences_whenMultipleOffencesAreInvalid_shouldReportEachOffencePath() {
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(999L, BUSINESS_UNIT_ID)).thenReturn(false);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(998L, BUSINESS_UNIT_ID)).thenReturn(false);

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
            """, BUSINESS_UNIT_ID)
        );

        String message = exception.getMessage();
        assertContains(message, "Draft account reference validation failed with 2 error(s):");
        assertContains(message, "account.offences[0].offence_id: offence id 999 does not exist");
        assertContains(message, "account.offences[1].offence_id: offence id 998 does not exist");

        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(999L, BUSINESS_UNIT_ID);
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(998L, BUSINESS_UNIT_ID);
    }

    @Test
    void validateReferences_whenSomeReferencesAreMissing_shouldReportAllFailures() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(false);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(false);
        when(resultRepository.findById("FO")).thenReturn(Optional.empty());
        when(resultRepository.findById("FVS")).thenReturn(Optional.empty());
        when(resultRepository.findById("COLLO")).thenReturn(Optional.empty());
        when(resultRepository.findById("NOENF")).thenReturn(Optional.empty());
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));

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
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(21L, BUSINESS_UNIT_ID);
        verify(offenceRepository).existsByOffenceIdAvailableToBusinessUnit(22L, BUSINESS_UNIT_ID);
        verify(resultRepository, times(4)).findById(org.mockito.ArgumentMatchers.anyString());
        verify(majorCreditorRepository, times(2)).existsById(anyLong());
    }

    @Test
    void validateReferences_whenImpositionResultIsNotAnImposition_shouldReportFailure() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeNonImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
    }

    @Test
    void validateReferences_whenImpositionResultIsInactive_shouldReportFailure() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(inactiveImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
    }

    @Test
    void validateReferences_whenPaymentTermsEnforcementResultIsNotAnEnforcement_shouldFail() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(ResultEntity.builder()
            .resultId("COLLO")
            .enforcement(false)
            .active(true)
            .build()));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.payment_terms.enforcements[0].result_id");
        assertContains(exception.getMessage(), "result id COLLO is not an enforcement result");
    }

    @Test
    void validateReferences_whenPaymentTermsEnforcementResultIsInactive_shouldFail() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), BUSINESS_UNIT_ID)).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(ResultEntity.builder()
            .resultId("COLLO")
            .enforcement(true)
            .active(false)
            .build()));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson(), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.payment_terms.enforcements[0].result_id");
        assertContains(exception.getMessage(), "result id COLLO is not an active result");
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
                    "result_id": "NOENF"
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

    private static ResultEntity activeEnforcementResult(String resultId) {
        return ResultEntity.builder()
            .resultId(resultId)
            .active(true)
            .enforcement(true)
            .build();
    }
}
