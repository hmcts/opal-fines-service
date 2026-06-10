package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
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
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountPartyServiceRemovePartyTests {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AmendmentRepositoryService amendmentRepositoryService;


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
            .auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        doNothing().when(amendmentRepositoryService)
            .auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short) 10, "posted", "Posted User", "CASE-REF",
                "ACCOUNT_ENQUIRY");

        RemoveDefendantAccountPartyRequest request = RemoveDefendantAccountPartyRequest.builder()
            .defendantAccountPartyId(99L)
            .build();

        RemoveDefendantAccountPartyResponse response = service.removeDefendantAccountParty(
            1L,
            5L,
            (short) 10,
            "businessUser",
            "posted",
            "Posted User",
            "1",
            request);

        assertEquals("5", response.getDefendantAccountPartyId());
        assertEquals(BigInteger.valueOf(2L), response.getVersion());
        verify(amendmentRepositoryService).auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentRepositoryService).auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short) 10,
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

        RemoveDefendantAccountPartyRequest request = RemoveDefendantAccountPartyRequest.builder()
            .defendantAccountPartyId(123L)
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(1L, 5L, (short) 10,
                                                "businessUser", "posted", "Posted User", "1", request));
    }

    @Test
    void removeDefendantAccountParty_whenBusinessUnitMismatch_throwsEntityNotFoundException() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(
                1L, 5L, (short) 11, "businessUser", "posted", "Posted User", "1", null));

        assertEquals("Defendant Account not found in business unit. Defendant Account: 1 Business Unit: 11",
            exception.getMessage());
        verify(defendantAccountRepositoryService).findById(1L);
        verify(amendmentRepositoryService, never()).auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        verify(defendantAccountRepositoryService, never()).saveAndFlush(account);
    }

    @Test
    void removeDefendantAccountParty_whenAssociationMissing_throwsEntityNotFoundException() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.removeDefendantAccountParty(
                1L, 999L, (short) 10, "businessUser", "posted", "Posted User", "1", null));

        assertEquals("Defendant Account Party not found for accountId=1, partyId=999", exception.getMessage());
        verify(defendantAccountRepositoryService).findById(1L);
        verify(amendmentRepositoryService).auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentRepositoryService, never())
            .auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short) 10, "posted", "Posted User", "CASE-REF",
                "ACCOUNT_ENQUIRY");
        verify(defendantAccountRepositoryService, never()).saveAndFlush(account);
    }
}
