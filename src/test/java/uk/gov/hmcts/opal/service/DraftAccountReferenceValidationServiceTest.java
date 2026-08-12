package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
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

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @Mock
    private ProsecutorRepository prosecutorRepository;

    @InjectMocks
    private DraftAccountReferenceValidationService service;

    @Test
    void validateReferences_whenAllReferencesExist_shouldPass() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(true);
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
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(false);
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
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(true);
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
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(true);
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
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(true);
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
        when(
            offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID))
        ).thenReturn(true);
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

    @Test
    void validateReferences_whenNewFineOriginatorMatchesLja_shouldPass() {
        when(localJusticeAreaRepository.findById((short)32001))
            .thenReturn(Optional.of(localJusticeArea((short)32001, "Draft Account Validation LJA",
                LocalJusticeAreaType.LJA)));

        assertDoesNotThrow(() -> service.validateReferences(originatorAccountJson(
            DraftAccountType.FINE,
            "NEW",
            32001L,
            "Draft Account Validation LJA"
        ), BUSINESS_UNIT_ID));

        verify(localJusticeAreaRepository).findById((short)32001);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenNewFineOriginatorMatchesCrwcrt_shouldPass() {
        when(localJusticeAreaRepository.findById((short)32002))
            .thenReturn(Optional.of(localJusticeArea((short)32002, "Draft Account Validation Crown Court",
                LocalJusticeAreaType.CRWCRT)));

        assertDoesNotThrow(() -> service.validateReferences(originatorAccountJson(
            DraftAccountType.FINE,
            "NEW",
            32002L,
            "Draft Account Validation Crown Court"
        ), BUSINESS_UNIT_ID));

        verify(localJusticeAreaRepository).findById((short)32002);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenNewConditionalCautionOriginatorMatchesProsecutor_shouldPass() {
        when(prosecutorRepository.findById(32010L))
            .thenReturn(Optional.of(prosecutor(32010L, "Draft Account Validation Prosecutor")));

        assertDoesNotThrow(() -> service.validateReferences(originatorAccountJson(
            DraftAccountType.CONDITIONAL_CAUTION,
            "NEW",
            32010L,
            "Draft Account Validation Prosecutor"
        ), BUSINESS_UNIT_ID));

        verify(prosecutorRepository).findById(32010L);
        verifyNoInteractions(localJusticeAreaRepository);
    }

    @Test
    void validateReferences_whenFixedPenaltyOriginatorMatchesProsecutor_shouldPass() {
        when(prosecutorRepository.findById(32010L))
            .thenReturn(Optional.of(prosecutor(32010L, "Draft Account Validation Prosecutor")));

        assertDoesNotThrow(() -> service.validateReferences(originatorAccountJson(
            DraftAccountType.FIXED_PENALTY,
            "FP",
            32010L,
            "Draft Account Validation Prosecutor"
        ), BUSINESS_UNIT_ID));

        verify(prosecutorRepository).findById(32010L);
        verifyNoInteractions(localJusticeAreaRepository);
    }

    @Test
    void validateReferences_whenOriginatorIdIsUnknown_shouldFail() {
        when(localJusticeAreaRepository.findById((short)32004)).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32004L,
                "Unknown LJA"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.originator_id: local justice area id 32004 does not exist");
        verify(localJusticeAreaRepository).findById((short)32004);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenOriginatorExistsInWrongSource_shouldFail() {
        when(localJusticeAreaRepository.findById((short)1111)).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                1111L,
                "Draft Account Validation Prosecutor"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.originator_id: local justice area id 1111 does not exist");
        verify(localJusticeAreaRepository).findById((short)1111);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenOriginatorIdMatchesDisallowedLjaType_shouldFail() {
        when(localJusticeAreaRepository.findById((short)32003))
            .thenReturn(Optional.of(localJusticeArea((short)32003, "Draft Account Validation SJ Court",
                LocalJusticeAreaType.SJCRT)));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32003L,
                "Draft Account Validation SJ Court"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.originator_id: local justice area id 32003 does not exist");
    }

    @Test
    void validateReferences_whenOriginatorNameDoesNotMatch_shouldFail() {
        when(localJusticeAreaRepository.findById((short)32001))
            .thenReturn(Optional.of(localJusticeArea((short)32001, "Draft Account Validation LJA",
                LocalJusticeAreaType.LJA)));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32001L,
                "Wrong LJA Name"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(),
            "$.originator_name: originator name 'Wrong LJA Name' does not match local justice area name "
                + "'Draft Account Validation LJA' for id 32001");
    }

    @Test
    void validateReferences_whenOriginatorCombinationIsUnsupported_shouldFail() {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE,
                "FP",
                32010L,
                "Draft Account Validation Prosecutor"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(),
            "$.originator_type: unsupported originator/account type combination: originator_type FP, "
                + "account_type Fine");
        verifyNoInteractions(localJusticeAreaRepository, prosecutorRepository);
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

    private static String originatorAccountJson(DraftAccountType accountType,
                                                String originatorType,
                                                Long originatorId,
                                                String originatorName) {
        return """
            {
              "account_type": "%s",
              "originator_type": "%s",
              "originator_id": %d,
              "originator_name": "%s"
            }
            """.formatted(accountType.getLabel(), originatorType, originatorId, originatorName);
    }

    private static LocalJusticeAreaEntity localJusticeArea(Short id, String name, LocalJusticeAreaType type) {
        return LocalJusticeAreaEntity.builder()
            .localJusticeAreaId(id)
            .name(name)
            .ljaType(type)
            .build();
    }

    private static ProsecutorEntity prosecutor(Long id, String name) {
        return ProsecutorEntity.builder()
            .prosecutorId(id)
            .name(name)
            .build();
    }
}
