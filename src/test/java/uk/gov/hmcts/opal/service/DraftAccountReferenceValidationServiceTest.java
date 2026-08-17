package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;

@ExtendWith(MockitoExtension.class)
class DraftAccountReferenceValidationServiceTest {

    private static final short BUSINESS_UNIT_ID = 78;
    private static final long VALID_MAJOR_CREDITOR_ID = 780000000041L;
    private static final long OTHER_BUSINESS_UNIT_MAJOR_CREDITOR_ID = 770000000105L;

    @Mock
    private CourtLiteRepository courtLiteRepository;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @Mock
    private ProsecutorRepository prosecutorRepository;

    @InjectMocks
    private DraftAccountReferenceValidationService service;

    @BeforeEach
    void setUp() {
        lenient().when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        lenient().when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID)))
            .thenReturn(true);
    }

    @Test
    void validateReferences_whenAllReferencesExist_shouldPass() {
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson()));
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenOffenceDoesNotExist_shouldReportOffencePath() {
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(999L, BUSINESS_UNIT_ID)).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(
                BUSINESS_UNIT_ID,
                """
                    {
                      "offences": [
                        {
                          "offence_id": 999
                        }
                      ]
                    }
                    """
            )
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
            () -> service.validateReferences(
                BUSINESS_UNIT_ID,
                """
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
                    """
            )
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
        when(offenceRepository.existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID)))
            .thenReturn(false);
        when(resultRepository.findById("FO")).thenReturn(Optional.empty());
        when(resultRepository.findById("FVS")).thenReturn(Optional.empty());
        when(resultRepository.findById("COLLO")).thenReturn(Optional.empty());
        when(resultRepository.findById("NOENF")).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson())
        );

        String message = exception.getMessage();
        assertContains(message, "$.enforcement_court_id");
        assertContains(message, "account.offences[0].offence_id");
        assertContains(message, "$.offences[0].imposing_court_id");
        assertContains(message, "$.offences[0].impositions[0].result_id");
        assertContains(message, "account.offences[1].offence_id");
        assertContains(message, "$.offences[1].imposing_court_id");
        assertContains(message, "$.offences[1].impositions[0].result_id");
        assertContains(message, "$.payment_terms.enforcements[0].result_id");
        assertContains(message, "$.payment_terms.enforcements[1].result_id");

        verify(courtLiteRepository, times(3)).existsById(anyLong());
        verify(offenceRepository, times(2)).existsByOffenceIdAvailableToBusinessUnit(anyLong(), eq(BUSINESS_UNIT_ID));
        verify(resultRepository, times(4)).findById(anyString());
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenImpositionResultIsNotAnImposition_shouldReportFailure() {
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeNonImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson())
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
        assertContains(exception.getMessage(), "result id FO is not an imposition result");
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenImpositionResultIsInactive_shouldReportFailure() {
        when(resultRepository.findById("FO")).thenReturn(Optional.of(inactiveImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(activeEnforcementResult("COLLO")));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson())
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
        assertContains(exception.getMessage(), "result id FO is not active");
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenCfResultHasCentralFundCreditor_shouldPass() {
        stubResult("FO", ImpositionCreditor.CF);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountType(
            BUSINESS_UNIT_ID,
            CreditorAccountType.CF
        )).thenReturn(true);

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FO", null, false)));
    }

    @Test
    void validateReferences_whenCfResultMissingCentralFundCreditor_shouldFailOnMajorCreditorPath() {
        stubResult("FO", ImpositionCreditor.CF);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountType(
            BUSINESS_UNIT_ID,
            CreditorAccountType.CF
        )).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FO", null, false))
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].major_creditor_id");
        assertContains(exception.getMessage(), "no central fund creditor account exists for business unit 78");
    }

    @Test
    void validateReferences_whenCpsResultHasProsecutionServiceCreditor_shouldPass() {
        stubResult("FCPC", ImpositionCreditor.CPS);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndProsecutionService(
            BUSINESS_UNIT_ID,
            CreditorAccountType.MJ,
            true
        )).thenReturn(true);

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCPC", null, false)));
    }

    @Test
    void validateReferences_whenCpsResultMissingProsecutionServiceCreditor_shouldFailOnMajorCreditorPath() {
        stubResult("FCPC", ImpositionCreditor.CPS);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndProsecutionService(
            BUSINESS_UNIT_ID,
            CreditorAccountType.MJ,
            true
        )).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCPC", null, false))
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].major_creditor_id");
        assertContains(exception.getMessage(), "no prosecution service creditor account exists for business unit 78");
    }

    @Test
    void validateReferences_whenNotCpsResultHasValidMajorCreditor_shouldPass() {
        stubResult("FCOST", ImpositionCreditor.NOT_CPS);
        when(creditorAccountRepository
            .existsByBusinessUnitIdAndCreditorAccountTypeAndProsecutionServiceAndMajorCreditorId(
                BUSINESS_UNIT_ID,
                CreditorAccountType.MJ,
                false,
                VALID_MAJOR_CREDITOR_ID
            )).thenReturn(true);

        assertDoesNotThrow(
            () -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCOST", VALID_MAJOR_CREDITOR_ID, false))
        );
    }

    @Test
    void validateReferences_whenMajorCreditorBelongsToAnotherBusinessUnit_shouldFail() {
        stubResult("FCOMP", ImpositionCreditor.ANY);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndMajorCreditorId(
            BUSINESS_UNIT_ID,
            CreditorAccountType.MJ,
            OTHER_BUSINESS_UNIT_MAJOR_CREDITOR_ID
        )).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(
                BUSINESS_UNIT_ID,
                accountJson("FCOMP", OTHER_BUSINESS_UNIT_MAJOR_CREDITOR_ID, false)
            )
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].major_creditor_id");
        assertContains(
            exception.getMessage(),
            "major creditor id 770000000105 is not valid for business unit 78 and result creditor rule Any"
        );
    }

    @Test
    void validateReferences_whenAnyResultHasValidMajorCreditor_shouldPass() {
        stubResult("FCOMP", ImpositionCreditor.ANY);
        when(creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndMajorCreditorId(
            BUSINESS_UNIT_ID,
            CreditorAccountType.MJ,
            VALID_MAJOR_CREDITOR_ID
        )).thenReturn(true);

        assertDoesNotThrow(
            () -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCOMP", VALID_MAJOR_CREDITOR_ID, false))
        );
    }

    @Test
    void validateReferences_whenAnyResultHasNoMajorCreditorAndNoMinorCreditor_shouldFailOnMinorCreditorPath() {
        stubResult("FCOMP", ImpositionCreditor.ANY);

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCOMP", null, false))
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].minor_creditor");
        assertContains(
            exception.getMessage(),
            "a minor creditor or valid major creditor id is required for result creditor rule Any"
        );
    }

    @Test
    void validateReferences_whenAnyResultHasNoMajorCreditorAndMinorCreditor_shouldPass() {
        stubResult("FCOMP", ImpositionCreditor.ANY);

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, accountJson("FCOMP", null, true)));
    }

    @Test
    void validateReferences_whenResultReferenceIsMissing_shouldReportResultPathOnly() {
        when(resultRepository.findById("NOT-A-RESULT")).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(
                BUSINESS_UNIT_ID,
                accountJson("NOT-A-RESULT", VALID_MAJOR_CREDITOR_ID, false)
            )
        );

        assertContains(exception.getMessage(), "$.offences[0].impositions[0].result_id");
        verifyNoInteractions(creditorAccountRepository);
    }

    private void stubResult(String resultId, ImpositionCreditor impositionCreditor) {
        when(resultRepository.findById(resultId)).thenReturn(
            Optional.of(
                ResultEntity.builder()
                    .resultId(resultId)
                    .active(true)
                    .imposition(true)
                    .impositionCreditor(impositionCreditor)
                    .build()
            )
        );
    }

    @Test
    void validateReferences_whenPaymentTermsEnforcementResultIsNotAnEnforcement_shouldFail() {
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(ResultEntity.builder()
            .resultId("COLLO")
            .enforcement(false)
            .active(true)
            .build()));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson())
        );

        assertContains(exception.getMessage(), "$.payment_terms.enforcements[0].result_id");
        assertContains(exception.getMessage(), "result id COLLO is not an enforcement result");
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenPaymentTermsEnforcementResultIsInactive_shouldFail() {
        when(resultRepository.findById("FO")).thenReturn(Optional.of(activeImpositionResult("FO")));
        when(resultRepository.findById("FVS")).thenReturn(Optional.of(activeImpositionResult("FVS")));
        when(resultRepository.findById("COLLO")).thenReturn(Optional.of(ResultEntity.builder()
            .resultId("COLLO")
            .enforcement(true)
            .active(false)
            .build()));
        when(resultRepository.findById("NOENF")).thenReturn(Optional.of(activeEnforcementResult("NOENF")));

        InvalidReferenceValidationException exception = assertThrows(
            InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, validAccountJson())
        );

        assertContains(exception.getMessage(), "$.payment_terms.enforcements[0].result_id");
        assertContains(exception.getMessage(), "result id COLLO is not an active result");
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void validateReferences_whenNewFineOriginatorMatchesLja_shouldPass() {
        when(localJusticeAreaRepository.findById((short)32001))
            .thenReturn(Optional.of(localJusticeArea((short)32001, "Draft Account Validation LJA",
                LocalJusticeAreaType.LJA)));

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
            DraftAccountType.FINE,
            "NEW",
            32001L,
            "Draft Account Validation LJA"
        )));

        verify(localJusticeAreaRepository).findById((short)32001);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenNewFineOriginatorMatchesCrwcrt_shouldPass() {
        when(localJusticeAreaRepository.findById((short)32002))
            .thenReturn(Optional.of(localJusticeArea((short)32002, "Draft Account Validation Crown Court",
                LocalJusticeAreaType.CRWCRT)));

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
            DraftAccountType.FINE,
            "NEW",
            32002L,
            "Draft Account Validation Crown Court"
        )));

        verify(localJusticeAreaRepository).findById((short)32002);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenNewConditionalCautionOriginatorMatchesProsecutor_shouldPass() {
        when(prosecutorRepository.findById(32010L))
            .thenReturn(Optional.of(prosecutor(32010L, "Draft Account Validation Prosecutor")));

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
            DraftAccountType.CONDITIONAL_CAUTION,
            "NEW",
            32010L,
            "Draft Account Validation Prosecutor"
        )));

        verify(prosecutorRepository).findById(32010L);
        verifyNoInteractions(localJusticeAreaRepository);
    }

    @Test
    void validateReferences_whenFixedPenaltyOriginatorMatchesProsecutor_shouldPass() {
        when(prosecutorRepository.findById(32010L))
            .thenReturn(Optional.of(prosecutor(32010L, "Draft Account Validation Prosecutor")));

        assertDoesNotThrow(() -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
            DraftAccountType.FIXED_PENALTY,
            "FP",
            32010L,
            "Draft Account Validation Prosecutor"
        )));

        verify(prosecutorRepository).findById(32010L);
        verifyNoInteractions(localJusticeAreaRepository);
    }

    @Test
    void validateReferences_whenOriginatorIdIsUnknown_shouldFail() {
        when(localJusticeAreaRepository.findById((short)32004)).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32004L,
                "Unknown LJA"
            )));

        assertContains(exception.getMessage(), "$.originator_id: local justice area id 32004 does not exist");
        verify(localJusticeAreaRepository).findById((short)32004);
        verifyNoInteractions(prosecutorRepository);
    }

    @Test
    void validateReferences_whenOriginatorExistsInWrongSource_shouldFail() {
        when(localJusticeAreaRepository.findById((short)1111)).thenReturn(Optional.empty());

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                1111L,
                "Draft Account Validation Prosecutor"
            )));

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
            () -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32003L,
                "Draft Account Validation SJ Court"
            )));

        assertContains(exception.getMessage(), "$.originator_id: local justice area id 32003 does not exist");
    }

    @Test
    void validateReferences_whenOriginatorNameDoesNotMatch_shouldFail() {
        when(localJusticeAreaRepository.findById((short)32001))
            .thenReturn(Optional.of(localJusticeArea((short)32001, "Draft Account Validation LJA",
                LocalJusticeAreaType.LJA)));

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
                DraftAccountType.FINE,
                "NEW",
                32001L,
                "Wrong LJA Name"
            )));

        assertContains(exception.getMessage(),
            "$.originator_name: originator name 'Wrong LJA Name' does not match local justice area name "
                + "'Draft Account Validation LJA' for id 32001");
    }

    @Test
    void validateReferences_whenOriginatorCombinationIsUnsupported_shouldFail() {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(BUSINESS_UNIT_ID, originatorAccountJson(
                DraftAccountType.FINE,
                "FP",
                32010L,
                "Draft Account Validation Prosecutor"
            )));

        assertContains(exception.getMessage(),
            "$.originator_type: unsupported originator/account type combination: originator_type FP, "
                + "account_type Fine");
        verifyNoInteractions(localJusticeAreaRepository, prosecutorRepository);
    }

    @Test
    void validateReferences_whenAccountTypeIsInvalid_shouldFail() {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                "Unknown",
                "NEW",
                32010L,
                "Draft Account Validation Prosecutor"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.account_type: unsupported account type 'Unknown'");
        verifyNoInteractions(localJusticeAreaRepository, prosecutorRepository);
    }

    @Test
    void validateReferences_whenOriginatorTypeIsInvalid_shouldFail() {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(originatorAccountJson(
                DraftAccountType.FINE.getLabel(),
                "CASE",
                32010L,
                "Draft Account Validation Prosecutor"
            ), BUSINESS_UNIT_ID));

        assertContains(exception.getMessage(), "$.originator_type: unsupported originator type 'CASE'");
        verifyNoInteractions(localJusticeAreaRepository, prosecutorRepository);
    }

    private static void assertContains(String message, String fragment) {
        org.junit.jupiter.api.Assertions.assertTrue(
            message.contains(fragment),
            () -> "Expected message to contain: " + fragment + "\nActual message:\n" + message
        );
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

    private static String accountJson(String resultId, Long majorCreditorId, boolean minorCreditorPresent) {
        String majorCreditorValue = majorCreditorId == null ? "null" : majorCreditorId.toString();
        String minorCreditorValue = minorCreditorPresent ? "{ \"organisation_name\": \"Acme\" }" : "null";

        return """
            {
              "enforcement_court_id": 11,
              "offences": [
                {
                  "offence_id": 21,
                  "imposing_court_id": 31,
                  "impositions": [
                    {
                      "result_id": "%s",
                      "major_creditor_id": %s,
                      "minor_creditor": %s
                    }
                  ]
                }
              ]
            }
            """.formatted(resultId, majorCreditorValue, minorCreditorValue);
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
        return originatorAccountJson(accountType.getLabel(), originatorType, originatorId, originatorName);
    }

    private static String originatorAccountJson(String accountType,
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
            """.formatted(accountType, originatorType, originatorId, originatorName);
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
