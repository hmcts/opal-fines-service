package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(offenceRepository.existsById(anyLong())).thenReturn(true);
        when(resultRepository.existsById(anyString())).thenReturn(true);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(true);

        assertDoesNotThrow(() -> service.validateReferences(validAccountJson()));
    }

    @Test
    void validateReferences_whenSomeReferencesAreMissing_shouldReportAllFailures() {
        when(courtLiteRepository.existsById(anyLong())).thenReturn(false);
        when(offenceRepository.existsById(anyLong())).thenReturn(false);
        when(resultRepository.existsById(anyString())).thenReturn(false);
        when(majorCreditorRepository.existsById(anyLong())).thenReturn(false);

        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences(validAccountJson()));

        String message = exception.getMessage();
        assertContains(message, "$.enforcement_court_id");
        assertContains(message, "$.offences[0].offence_id");
        assertContains(message, "$.offences[0].imposing_court_id");
        assertContains(message, "$.offences[0].impositions[0].result_id");
        assertContains(message, "$.offences[0].impositions[0].major_creditor_id");
        assertContains(message, "$.offences[1].offence_id");
        assertContains(message, "$.offences[1].imposing_court_id");
        assertContains(message, "$.offences[1].impositions[0].result_id");
        assertContains(message, "$.offences[1].impositions[0].major_creditor_id");
        assertContains(message, "$.payment_terms.enforcements[0].result_id");
        assertContains(message, "$.payment_terms.enforcements[1].result_id");

        verify(courtLiteRepository, times(3)).existsById(anyLong());
        verify(offenceRepository, times(2)).existsById(anyLong());
        verify(resultRepository, times(4)).existsById(anyString());
        verify(majorCreditorRepository, times(2)).existsById(anyLong());
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
        )));

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
        )));

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
        )));

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
        )));

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
            )));

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
            () -> service.validateReferences(originatorAccountJson(
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
            () -> service.validateReferences(originatorAccountJson(
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
            () -> service.validateReferences(originatorAccountJson(
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
            )));

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
            )));

        assertContains(exception.getMessage(), "$.originator_type: unsupported originator type 'CASE'");
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
                      "result_id": "PRIS",
                      "major_creditor_id": 41
                    }
                  ]
                },
                {
                  "offence_id": 22,
                  "imposing_court_id": 32,
                  "impositions": [
                    {
                      "result_id": "NOENF",
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
