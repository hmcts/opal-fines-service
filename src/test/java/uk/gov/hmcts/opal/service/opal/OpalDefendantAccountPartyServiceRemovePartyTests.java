package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountPartiesRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountPartyServiceRemovePartyTests {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AmendmentRepositoryService amendmentRepositoryService;


    @Mock
    private DefendantAccountPartiesRepositoryService defendantAccountPartiesRepositoryService;

    @InjectMocks
    private OpalDefendantAccountPartyService service;

    private DefendantAccountEntity account;
    private DefendantAccountPartiesEntity partyAssociation;

    @BeforeEach
    void setUp() {
        BusinessUnitFullEntity businessUnit = new BusinessUnitFullEntity();
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
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account, account);
        when(defendantAccountRepositoryService.saveAndFlush(account)).thenReturn(account);
        doNothing().when(amendmentRepositoryService)
            .auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        doNothing().when(amendmentRepositoryService)
            .auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short) 10, "posted", "CASE-REF",
                "ACCOUNT_ENQUIRY");
        doNothing().when(defendantAccountPartiesRepositoryService).delete(partyAssociation);

        DefendantAccountParty request = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("99").build())
            .build();

        RemoveDefendantAccountPartyResponse response = service.removeDefendantAccountParty(
            1L,
            5L,
            (short) 10,
            "businessUser",
            "1",
            "posted",
            request);

        assertEquals("5", response.getDefendantAccountPartyId());
        assertEquals(BigInteger.valueOf(2L), response.getVersion());
        verify(defendantAccountPartiesRepositoryService).delete(partyAssociation);
        verify(amendmentRepositoryService).auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentRepositoryService).auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short) 10,
            "posted", "CASE-REF", "ACCOUNT_ENQUIRY");
        verify(defendantAccountRepositoryService, times(2)).findById(1L);
        verify(defendantAccountRepositoryService).saveAndFlush(account);
    }

    @Test
    void removeDefendantAccountParty_whenPartyIdMismatch_throwsIllegalArgumentException() {
        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        DefendantAccountParty request = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("123").build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            service.removeDefendantAccountParty(1L, 5L, (short) 10, "businessUser", "1", "posted", request));
    }
}
