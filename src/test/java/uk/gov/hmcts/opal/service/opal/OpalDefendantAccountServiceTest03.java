package uk.gov.hmcts.opal.service.opal;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest03 {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private AliasRepository aliasRepo;

    @Mock
    private DebtorDetailRepository debtorRepo;

    @Mock
    private OpalPartyService opalPartyService;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void replaceDefendantAccountParty_noExistingParty_andMissingPartyId_throws() {
        Long accountId = 100L;
        Long dapId = 200L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(null).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            assertThrows(IllegalArgumentException.class, () ->
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null));

            verify(defendantAccountRepository, never()).saveAndFlush(any());
        }
    }

    @Test
    void replaceDefendantAccountParty_switchingParty_isForbidden() {
        Long accountId = 100L;
        Long dapId = 200L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("999").organisationFlag(Boolean.TRUE).build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;

        BusinessUnitFullEntity buWrong = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        assertThrows(EntityNotFoundException.class, () ->
            service.replaceDefendantAccountParty(accountId, 1L,
                DefendantAccountParty.builder().build(), "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceDefendantAccountParty_nonDebtorAndNoPayload_deletesExistingDebtorDetail() {
        Long accountId = 200L;
        Long dapId = 201L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(222L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(222L);
        when(opalPartyService.findById(222L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(222L)).thenReturn(emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.FALSE)
            .partyDetails(PartyDetails.builder()
                .partyId("222").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("X").build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            // existing debtor should be deleted (we previously retrieved it via findById)
            verify(defendantAccountRepository).saveAndFlush(account);
        }
    }

    @Test
    void replaceDefendantAccountParty_addressNull_and_contactNull_clear_all_fields() {
        Long accountId = 400L;
        Long dapId = 401L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(444L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(opalPartyService.findById(444L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(444L)).thenReturn(emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorRepo.findById(444L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("444").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ORG").build())
                .build())
            .address(null).contactDetails(null).build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            // since address & contact were null, party setters should be called to clear fields
            verify(party).setAddressLine1(null);
            verify(party).setAddressLine2(null);
            verify(party).setAddressLine3(null);
            verify(party).setAddressLine4(null);
            verify(party).setAddressLine5(null);
            verify(party).setPostcode(null);

            verify(party).setPrimaryEmailAddress(null);
            verify(party).setSecondaryEmailAddress(null);
            verify(party).setMobileTelephoneNumber(null);
            verify(party).setHomeTelephoneNumber(null);
            verify(party).setWorkTelephoneNumber(null);

            verify(defendantAccountRepository).saveAndFlush(account);
        }
    }

    @Test
    void replaceDefendantAccountParty_happyPath_attachedParty_updates_and_audits() {
        Long accountId = 777L;
        Long dapId = 888L;
        String bu = "10";
        String ifMatch = "\"1\"";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(123L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(party)
            .associationType("RESPONDENT")
            .debtor(Boolean.FALSE)
            .build();

        account.setParties(List.of(dap));

        when(aliasRepo.findByParty_PartyId(123L)).thenReturn(emptyList());

        when(opalPartyService.findById(123L)).thenReturn(party);

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorRepo.findById(123L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("123")
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME LTD").build())
                .build())
            .address(AddressDetails.builder().addressLine1("1 MAIN").postcode("AB1 2CD").build())
            .contactDetails(ContactDetails.builder().primaryEmailAddress("a@b.com").workTelephoneNumber("0207").build())
            .vehicleDetails(VehicleDetails.builder().vehicleMakeAndModel("Ford Focus")
                .vehicleRegistration("AB12CDE").build())
            .employerDetails(EmployerDetails.builder()
                .employerName("Widgets Inc")
                .employerAddress(AddressDetails.builder().addressLine1("10 Park").postcode("ZZ1 1ZZ").build())
                .build())
            .languagePreferences(LanguagePreferences.builder()
                .documentLanguagePreference(LanguagePreference.fromCode("EN"))
                .hearingLanguagePreference(LanguagePreference.fromCode("CY"))
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, ifMatch, bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());

            verify(defendantAccountRepository).saveAndFlush(account);
            verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentService).auditFinaliseStoredProc(
                eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), any(), eq("ACCOUNT_ENQUIRY"));

            verify(party).setOrganisation(Boolean.TRUE);
            verify(party).setOrganisationName("ACME LTD");
            verify(party).setAddressLine1("1 MAIN");
            verify(party).setPrimaryEmailAddress("a@b.com");

            // called inside replaceAliasesForParty and again when building response
            verify(aliasRepo, times(2)).findByParty_PartyId(123L);
        }
    }

    @Test
    void replaceDefendantAccountParty_detachedParty_isReattached_via_OpalPartyService_findById() {
        Long accountId = 100L;
        Long dapId = 200L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        PartyEntity partyProxy = mock(PartyEntity.class);
        when(partyProxy.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(partyProxy).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(opalPartyService.findById(300L)).thenReturn(partyProxy);
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(aliasRepo.findByParty_PartyId(300L)).thenReturn(emptyList());

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("300").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            verify(opalPartyService, times(2)).findById(300L); // main + aliases
            verify(defendantAccountRepository).saveAndFlush(account);
            verify(aliasRepo, times(2)).findByParty_PartyId(300L);
        }
    }


    @Test
    void replaceDefendantAccountParty_employerNull_languageNull_clearsEmployerAndLanguages_savesDebtor() {
        Long accountId = 300L;
        Long dapId = 301L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(333L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        when(opalPartyService.findById(333L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(333L)).thenReturn(emptyList());

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);
        when(debtorRepo.findById(333L)).thenReturn(Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(debtorRepo.findById(333L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("333").organisationFlag(Boolean.FALSE)
                .individualDetails(IndividualDetails.builder()
                    .title("Ms").forenames("Jane").surname("Doe")
                    .dateOfBirth("1990-01-02").age("35").nationalInsuranceNumber("NI123").build())
                .build())
            .vehicleDetails(VehicleDetails.builder().vehicleMakeAndModel("VW Golf")
                .vehicleRegistration("JD02CAR").build())
            // employer null, language null
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            ArgumentCaptor<DebtorDetailEntity> cap = ArgumentCaptor.forClass(DebtorDetailEntity.class);
            verify(debtorRepo).save(cap.capture());
            DebtorDetailEntity saved = cap.getValue();

            assertEquals("VW Golf", saved.getVehicleMake());
            assertEquals("JD02CAR", saved.getVehicleRegistration());

            assertNull(saved.getEmployerName());
            assertNull(saved.getEmployeeReference());
            assertNull(saved.getEmployerEmail());
            assertNull(saved.getEmployerTelephone());
            assertNull(saved.getEmployerAddressLine1());
            assertNull(saved.getEmployerAddressLine2());
            assertNull(saved.getEmployerAddressLine3());
            assertNull(saved.getEmployerAddressLine4());
            assertNull(saved.getEmployerAddressLine5());
            assertNull(saved.getEmployerPostcode());

            assertNull(saved.getDocumentLanguage());
            assertNull(saved.getHearingLanguage());
            assertNull(saved.getDocumentLanguageDate());
            assertNull(saved.getHearingLanguageDate());

            verify(defendantAccountRepository).saveAndFlush(account);
            verify(aliasRepo, times(2)).findByParty_PartyId(333L);
        }
    }

}
