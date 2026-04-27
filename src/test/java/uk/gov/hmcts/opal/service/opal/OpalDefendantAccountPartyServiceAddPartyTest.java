package uk.gov.hmcts.opal.service.opal;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.RecordType;
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
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.service.persistence.AliasRepositoryService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PartyRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountPartyServiceAddPartyTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AmendmentRepositoryService amendmentRepositoryService;

    @Mock
    private AliasRepositoryService aliasRepoService;

    @Mock
    private DebtorDetailRepositoryService debtorRepoService;

    @Mock
    private DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Mock
    private PartyRepositoryService partyRepositoryService;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountPartyService service;

    @Test
    void addDefendantAccountParty_happyPath_createsPartyAssociationAndAudits() {
        Long accountId = 777L;
        String bu = "10";
        String ifMatch = "\"1\"";

        BusinessUnitEntity buEnt = BusinessUnitEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        PartyEntity savedParty = PartyEntity.builder()
            .partyId(123L)
            .organisation(false)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .build();

        DefendantAccountPartiesEntity savedDap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(456L)
            .defendantAccount(account)
            .party(savedParty)
            .associationType(AssociationType.DEFENDANT)
            .debtor(Boolean.TRUE)
            .build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        when(partyRepositoryService.save(any(PartyEntity.class))).thenReturn(savedParty);
        when(partyRepositoryService.findById(123L)).thenReturn(savedParty);
        when(defendantAccountPartiesRepository.save(any(DefendantAccountPartiesEntity.class))).thenReturn(savedDap);
        when(aliasRepoService.findByPartyId(123L)).thenReturn(emptyList());
        when(debtorRepoService.findByPartyId(123L)).thenReturn(Optional.of(DebtorDetailEntity.builder()
            .partyId(123L)
            .vehicleMake("Ford Focus")
            .vehicleRegistration("AB12CDE")
            .build()));
        when(defendantAccountRepositoryService.saveAndFlush(account)).thenReturn(account);

        AddDefendantAccountPartyRequest req = AddDefendantAccountPartyRequest.builder()
            .defendantAccountParty(DefendantAccountParty.builder()
                .defendantAccountPartyType("Defendant")
                .isDebtor(Boolean.TRUE)
                .partyDetails(PartyDetails.builder()
                    .organisationFlag(Boolean.FALSE)
                    .individualDetails(IndividualDetails.builder()
                        .title("Mr")
                        .forenames("John")
                        .surname("Smith")
                        .dateOfBirth("1980-01-01")
                        .age("44")
                        .nationalInsuranceNumber("AB123456C")
                        .build())
                    .build())
                .address(AddressDetails.builder().addressLine1("1 Main Street").postcode("AB1 2CD").build())
                .contactDetails(ContactDetails.builder()
                    .primaryEmailAddress("john@example.com")
                    .mobileTelephoneNumber("07123456789")
                    .build())
                .vehicleDetails(VehicleDetails.builder()
                    .vehicleMakeAndModel("Ford Focus")
                    .vehicleRegistration("AB12CDE")
                    .build())
                .employerDetails(EmployerDetails.builder().employerName("Widgets Ltd").build())
                .languagePreferences(LanguagePreferences.builder()
                    .documentLanguagePreference(LanguagePreference.fromCode("EN"))
                    .hearingLanguagePreference(LanguagePreference.fromCode("CY"))
                    .build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), eq(
                "addDefendantAccountParty"))).thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.addDefendantAccountParty(accountId, bu, "bu-user-1", "tester", ifMatch, req);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            assertEquals("123", resp.getDefendantAccountParty().getPartyDetails().getPartyId());
            assertEquals("John", resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails()
                .getForenames());

            verify(partyRepositoryService).save(argThat(party ->
                !party.isOrganisation()
                    && "Mr".equals(party.getTitle())
                    && "John".equals(party.getForenames())
                    && "Smith".equals(party.getSurname())
                    && "1 Main Street".equals(party.getAddressLine1())
                    && "john@example.com".equals(party.getPrimaryEmailAddress())
            ));
            verify(defendantAccountPartiesRepository).save(argThat(dap ->
                dap.getDefendantAccount() == account
                    && dap.getParty() == savedParty
                    && dap.getAssociationType() == AssociationType.DEFENDANT
                    && Boolean.TRUE.equals(dap.getDebtor())
            ));
            verify(debtorRepoService).addDebtorDetail(
                eq(123L),
                argThat(v -> "Ford Focus".equals(v.getVehicleMakeAndModel())
                    && "AB12CDE".equals(v.getVehicleRegistration())),
                argThat(e -> "Widgets Ltd".equals(e.getEmployerName())),
                argThat(l -> l.getDocumentLanguagePreference() != null
                    && l.getHearingLanguagePreference() != null),
                eq(true)
            );
            verify(amendmentRepositoryService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentRepositoryService).auditFinaliseStoredProc(
                eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), any(), eq("ACCOUNT_ENQUIRY"));
            verify(defendantAccountRepositoryService).saveAndFlush(account);
        }
    }

    @Test
    void addDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;

        BusinessUnitEntity buWrong = BusinessUnitEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);

        AddDefendantAccountPartyRequest req = AddDefendantAccountPartyRequest.builder()
            .defendantAccountParty(DefendantAccountParty.builder()
                .defendantAccountPartyType("Defendant")
                .isDebtor(Boolean.FALSE)
                .partyDetails(PartyDetails.builder()
                    .organisationFlag(Boolean.TRUE)
                    .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                    .build())
                .build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.addDefendantAccountParty(accountId, "10", "bu-user-1", "tester", "\"1\"", req));

        verify(defendantAccountPartiesRepository, never()).save(any());
        verify(partyRepositoryService, never()).save(any());
    }
}
