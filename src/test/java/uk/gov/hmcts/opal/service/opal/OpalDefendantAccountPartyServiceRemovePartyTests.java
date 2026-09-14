package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyDetailsCommonStrict;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountPartyServiceRemovePartyTests {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AmendmentRepositoryService amendmentRepositoryService;

    @Mock
    private DefendantAccountControlValidator defendantAccountControlValidator;


    @InjectMocks
    private OpalDefendantAccountPartyService service;

    private DefendantAccountEntity account;
    private DefendantAccountPartiesEntity partyAssociation;

    @BeforeEach
    void setUp() {
        BusinessUnitEntity businessUnit = new BusinessUnitEntity();
        businessUnit.setBusinessUnitId((short) 10);

        account = new DefendantAccountEntity();
        account.setDefendantAccountId(1L);
        account.setBusinessUnit(businessUnit);
        account.setVersionNumber(1L);
        account.setProsecutorCaseReference("CASE-REF");

        PartyEntity party = PartyEntity.builder().partyId(99L).build();

        partyAssociation = new DefendantAccountPartiesEntity();
        partyAssociation.setDefendantAccountPartyId(5L);
        partyAssociation.setParty(party);

        account.setParties(new ArrayList<>(List.of(partyAssociation)));
    }

    @Test
    void removeDefendantAccountParty_whenValidRequest_deletesAssociationAndReturnsVersion() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);
        when(defendantAccountRepositoryService.saveAndFlush(account)).thenAnswer(invocation -> {
            account.setVersionNumber(2L);
            return account;
        });
        doNothing().when(amendmentRepositoryService)
            .auditInitialiseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        doNothing().when(amendmentRepositoryService)
            .auditFinaliseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS, (short) 10,
                "posted", "Posted User", "CASE-REF",
                "ACCOUNT_ENQUIRY");

        RemoveDefendantAccountPartyResponseDefendantAccount response = service.removeDefendantAccountParty(
            1L,
            5L,
            (short) 10,
            "businessUser",
            "posted",
            "Posted User",
            "1",
            validRequest());

        assertEquals("5", response.getDefendantAccountPartyId());
        assertEquals(BigInteger.valueOf(2L), response.getVersion());
        verify(amendmentRepositoryService).auditInitialiseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentRepositoryService)
            .auditFinaliseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS, (short) 10,
                "posted", "Posted User", "CASE-REF", "ACCOUNT_ENQUIRY");
        verify(defendantAccountRepositoryService).findById(1L);
        verify(defendantAccountRepositoryService).saveAndFlush(account);
        assertEquals(0, account.getParties().size());
    }

    @Test
    void removeDefendantAccountParty_whenDefendantAccountPartyIdMismatch_throwsEntityNotFound() {

        DefendantAccountPartiesEntity party = new DefendantAccountPartiesEntity();
        party.setDefendantAccountPartyId(999L);

        account.setParties(List.of(party));

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(1L, 5L, (short) 10,
                                                "businessUser", "posted", "Posted User", "1", validRequest()));
    }

    @Test
    void removeDefendantAccountParty_whenBusinessUnitMismatch_throwsEntityNotFoundException() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(
                1L, 5L, (short) 11, "businessUser", "posted", "Posted User", "1", validRequest()));

        assertEquals("Defendant Account not found in business unit 11",
            exception.getMessage());
        verify(defendantAccountRepositoryService).findById(1L);
        verify(amendmentRepositoryService, never())
            .auditInitialiseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        verify(defendantAccountRepositoryService, never()).saveAndFlush(account);
    }

    @Test
    void removeDefendantAccountParty_whenAccountControlsFail_throwsBeforeMutation() {
        UnprocessableException exception = new UnprocessableException("blocked");

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);
        doThrow(exception).when(defendantAccountControlValidator).validateCanMutateParty(account);

        UnprocessableException result = assertThrows(UnprocessableException.class, () ->
            service.removeDefendantAccountParty(1L, 5L, (short) 10, "businessUser", "posted", "1",
                                                validRequest()));

        assertEquals(exception, result);
        verify(defendantAccountControlValidator).validateCanMutateParty(account);
        verify(amendmentRepositoryService, never())
            .auditInitialiseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        verify(defendantAccountRepositoryService, never()).saveAndFlush(account);
        assertEquals(1, account.getParties().size());
    }

    @Test
    void removeDefendantAccountParty_whenAssociationMissing_throwsEntityNotFoundException() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(
                1L, 999L, (short) 10, "businessUser", "posted", "Posted User", "1", validRequest()));

        assertEquals("Defendant Account Party not found for accountId=1, partyId=999", exception.getMessage());
        verify(defendantAccountRepositoryService).findById(1L);
        verify(amendmentRepositoryService).auditInitialiseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentRepositoryService, never())
            .auditFinaliseStoredProc(1L, AssociatedRecordType.DEFENDANT_ACCOUNTS, (short) 10,
                "posted", "Posted User", "CASE-REF",
                "ACCOUNT_ENQUIRY");
        verify(defendantAccountRepositoryService, never()).saveAndFlush(account);
    }

    @Test
    void removeDefendantAccountParty_whenRequestBodyMissing_throwsJsonSchemaValidationException() {
        JsonSchemaValidationException exception = assertThrows(JsonSchemaValidationException.class, () ->
            service.removeDefendantAccountParty(
                1L, 5L, (short) 10, "businessUser", "posted", "Posted User", "1", null));

        assertEquals("Request body is required", exception.getMessage());
        verify(defendantAccountRepositoryService, never()).findById(1L);
    }

    @Test
    void removeDefendantAccountParty_whenRequestBodyDoesNotContainPartyReference_throwsJsonSchemaValidationException() {
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            RemoveDefendantAccountPartyRequestDefendantAccount.builder()
                .version(1L)
                .build();

        JsonSchemaValidationException exception = assertThrows(JsonSchemaValidationException.class, () ->
            service.removeDefendantAccountParty(
                1L, 5L, (short) 10, "businessUser", "posted", "Posted User", "1", request));

        assertEquals("defendant_account_party_id or party_details must be provided", exception.getMessage());
        verify(defendantAccountRepositoryService, never()).findById(1L);
    }

    @Test
    void removeDefendantAccountParty_whenPartyReferenceIsBlank_throwsJsonSchemaValidationException() {
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            RemoveDefendantAccountPartyRequestDefendantAccount.builder()
                .defendantAccountPartyId("")
                .build();

        JsonSchemaValidationException exception = assertThrows(JsonSchemaValidationException.class, () ->
            service.removeDefendantAccountParty(
                1L, 5L, (short) 10, "businessUser", "posted", "Posted User", "1", request));

        assertEquals("defendant_account_party_id must not be blank", exception.getMessage());
        verify(defendantAccountRepositoryService, never()).findById(1L);
    }

    private RemoveDefendantAccountPartyRequestDefendantAccount validRequest() {
        return RemoveDefendantAccountPartyRequestDefendantAccount.builder()
            .partyDetails(RemoveDefendantAccountPartyDetailsCommonStrict.builder()
                .partyId("99")
                .build())
            .build();
    }
}
