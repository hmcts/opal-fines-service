package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
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
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
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

    @InjectMocks
    private DraftAccountReferenceValidationService service;

    @BeforeEach
    void setUp() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(true);
        when(offenceRepository.existsById(anyLong())).thenReturn(true);
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
            Optional.of(ResultEntity.builder().resultId(resultId).impositionCreditor(impositionCreditor).build())
        );
    }

    private static void assertContains(String message, String fragment) {
        org.junit.jupiter.api.Assertions.assertTrue(
            message.contains(fragment),
            () -> "Expected message to contain: " + fragment + "\nActual message:\n" + message
        );
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
}
