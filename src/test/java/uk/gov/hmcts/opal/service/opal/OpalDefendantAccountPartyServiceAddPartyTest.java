package uk.gov.hmcts.opal.service.opal;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.mapper.response.DefendantAccountPartyEntityResponseMapper;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.service.persistence.AliasRepositoryService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PartyRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountPartyServiceAddPartyTest {

    @Spy
    private DefendantAccountPartyEntityResponseMapper defendantAccountPartyEntityResponseMapper =
        Mappers.getMapper(DefendantAccountPartyEntityResponseMapper.class);

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

    @Mock
    private DefendantAccountControlValidator defendantAccountControlValidator;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountPartyService service;

    @Test
    void addDefendantAccountParty_happyPath_createsPartyAssociationAndAudits() {
        // Arrange
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

        account.setParties(new java.util.ArrayList<>());

        PartyEntity savedParty = PartyEntity.builder()
            .partyId(123L)
            .organisation(false)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        when(partyRepositoryService.save(any(PartyEntity.class))).thenReturn(savedParty);
        when(debtorRepoService.findById(123L)).thenReturn(Optional.empty());
        when(aliasRepoService.findByPartyId(123L)).thenReturn(emptyList());
        when(debtorRepoService.findByPartyId(123L)).thenReturn(Optional.of(DebtorDetailEntity.builder()
            .partyId(123L)
            .vehicleMake("Ford Focus")
            .vehicleRegistration("AB12CDE")
            .build()));
        when(defendantAccountRepositoryService.saveAndFlush(account)).thenReturn(account);

        AddPartyRequestDefendantAccount req = AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(PartyDefendantAccount.builder()
                .defendantAccountPartyType(PartyDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(Boolean.TRUE)
                .partyDetails(PartyDetailsCommonStrict.builder()
                    .organisationFlag(Boolean.FALSE)
                    .individualDetails(IndividualDetailsCommonStrict.builder()
                        .title("Mr")
                        .forenames("John")
                        .surname("Smith")
                        .dateOfBirth("1980-01-01")
                        .age("44")
                        .nationalInsuranceNumber("AB123456C")
                        .build())
                    .build())
                .address(AddressDetailsCommonStrict.builder().addressLine1("1 Main Street").postcode("AB1 2CD").build())
                .contactDetails(PartyContactDetailsDefendantAccount.builder()
                    .primaryEmailAddress("john@example.com")
                    .mobileTelephoneNumber("07123456789")
                    .build())
                .vehicleDetails(PartyVehicleDetailsDefendantAccount.builder()
                    .vehicleMakeAndModel("Ford Focus")
                    .vehicleRegistration("AB12CDE")
                    .build())
                .employerDetails(PartyEmployerDetailsDefendantAccount.builder().employerName("Widgets Ltd").build())
                .languagePreferences(LanguagePreferencesCommonStrict.builder()
                    .documentLanguagePreference(languagePreference("EN"))
                    .hearingLanguagePreference(languagePreference("CY"))
                    .build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), eq(
                "addDefendantAccountParty"))).thenAnswer(i -> null);

            // Act
            PartyResponseDefendantAccount resp =
                service.addDefendantAccountParty(accountId, bu, "bu-user-1", "tester", "Tester Name", ifMatch, req);

            // Assert
            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            assertEquals("123", resp.getDefendantAccountParty().getPartyDetails().getPartyId());
            assertEquals("John", resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails()
                .get().getForenames().get());

            verify(partyRepositoryService).save(argThat(party ->
                !party.isOrganisation()
                    && "Mr".equals(party.getTitle())
                    && "John".equals(party.getForenames())
                    && "Smith".equals(party.getSurname())
                    && "1 Main Street".equals(party.getAddressLine1())
                    && "john@example.com".equals(party.getPrimaryEmailAddress())
            ));
            assertEquals(1, account.getParties().size());
            assertEquals(savedParty, account.getParties().getFirst().getParty());
            assertEquals(AssociationType.DEFENDANT, account.getParties().getFirst().getAssociationType());
            assertEquals(Boolean.TRUE, account.getParties().getFirst().getDebtor());
            verify(defendantAccountPartiesRepository, never()).save(any());
            verify(debtorRepoService).addDebtorDetail(
                eq(123L),
                argThat(v -> "Ford Focus".equals(v.getVehicleMakeAndModel().get())
                    && "AB12CDE".equals(v.getVehicleRegistration().get())),
                argThat(e -> "Widgets Ltd".equals(e.getEmployerName().get())),
                argThat(l -> l.getDocumentLanguagePreference().isPresent()
                    && l.getHearingLanguagePreference().isPresent())
            );
            verify(amendmentRepositoryService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentRepositoryService).auditFinaliseStoredProc(
                eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), eq("Tester Name"), any(), eq("ACCOUNT_ENQUIRY"));
            verify(defendantAccountRepositoryService).saveAndFlush(account);
        }
    }

    @Test
    void addDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;
        String businessUnitId = "10";

        BusinessUnitEntity buWrong = BusinessUnitEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        AddPartyRequestDefendantAccount request = validOrganisationRequest();

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.addDefendantAccountParty(
                accountId, businessUnitId, "bu-user-1", "tester", "Tester Name", "\"1\"", request));

        assertEquals("Defendant Account not found in business unit " + businessUnitId, exception.getMessage());
        verify(defendantAccountRepositoryService).findById(accountId);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_whenAccountControlsFail_throwsBeforeMutation() {
        Long accountId = 100L;
        String businessUnitId = "10";
        DefendantAccountEntity account = defendantAccount(accountId, Short.parseShort(businessUnitId), 1L);
        UnprocessableException exception = new UnprocessableException("blocked");

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        doThrow(exception).when(defendantAccountControlValidator).validateCanMutateParty(account);
        AddPartyRequestDefendantAccount request = validOrganisationRequest();

        UnprocessableException result = assertThrows(UnprocessableException.class, () ->
            service.addDefendantAccountParty(
                accountId, businessUnitId, "bu-user-1", "tester", "\"1\"", request));

        assertEquals(exception, result);
        verify(defendantAccountControlValidator).validateCanMutateParty(account);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_nullRequest_throws() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            service.addDefendantAccountParty(100L, "10", "bu-user-1", "tester", "Tester Name", "\"1\"", null));

        assertEquals("Request body is required", exception.getMessage());
        verifyNoInteractions(defendantAccountRepositoryService);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_missingDefendantAccountParty_throws() {
        AddPartyRequestDefendantAccount request = AddPartyRequestDefendantAccount.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            service.addDefendantAccountParty(100L, "10", "bu-user-1", "tester", "Tester Name", "\"1\"", request));

        assertEquals("Request body is required", exception.getMessage());
        verifyNoInteractions(defendantAccountRepositoryService);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_missingPartyDetails_throws() {
        AddPartyRequestDefendantAccount request = AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(PartyDefendantAccount.builder()
                .defendantAccountPartyType(PartyDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(Boolean.FALSE)
                .build())
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            service.addDefendantAccountParty(100L, "10", "bu-user-1", "tester", "Tester Name", "\"1\"", request));

        assertEquals("party_details.organisation_flag is required", exception.getMessage());
        verifyNoInteractions(defendantAccountRepositoryService);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_missingOrganisationFlag_throws() {
        AddPartyRequestDefendantAccount request = AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(PartyDefendantAccount.builder()
                .defendantAccountPartyType(PartyDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(Boolean.FALSE)
                .partyDetails(PartyDetailsCommonStrict.builder().build())
                .build())
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            service.addDefendantAccountParty(100L, "10", "bu-user-1", "tester", "Tester Name", "\"1\"", request));

        assertEquals("party_details.organisation_flag is required", exception.getMessage());
        verifyNoInteractions(defendantAccountRepositoryService);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_accountNotFound_throws() {
        Long accountId = 100L;
        EntityNotFoundException notFound =
            new EntityNotFoundException("Defendant Account not found with id: " + accountId);

        when(defendantAccountRepositoryService.findById(accountId)).thenThrow(notFound);
        AddPartyRequestDefendantAccount request = validOrganisationRequest();

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.addDefendantAccountParty(accountId, "10", "bu-user-1", "tester", "Tester Name", "\"1\"", request));

        assertEquals("Defendant Account not found with id: " + accountId, exception.getMessage());
        verify(defendantAccountRepositoryService).findById(accountId);
        verifyNoAddSideEffects();
    }

    @Test
    void addDefendantAccountParty_staleIfMatch_throws() {
        Long accountId = 100L;
        String businessUnitId = "10";
        DefendantAccountEntity account = defendantAccount(accountId, Short.parseShort(businessUnitId), 5L);

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        AddPartyRequestDefendantAccount request = validOrganisationRequest();

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            service.addDefendantAccountParty(
                accountId, businessUnitId, "bu-user-1", "tester", "Tester Name", "\"4\"", request));

        verify(defendantAccountRepositoryService).findById(accountId);
        verifyNoAddSideEffects();
    }

    private static AddPartyRequestDefendantAccount validOrganisationRequest() {
        return AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(PartyDefendantAccount.builder()
                .defendantAccountPartyType(PartyDefendantAccount.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(Boolean.FALSE)
                .partyDetails(PartyDetailsCommonStrict.builder()
                    .organisationFlag(Boolean.TRUE)
                    .organisationDetails(OrganisationDetailsCommonStrict.builder().organisationName("ACME").build())
                    .build())
                .build())
            .build();
    }

    private static DefendantAccountEntity defendantAccount(Long accountId, short businessUnitId, long version) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(businessUnitId).build())
            .versionNumber(version)
            .build();
    }

    private static LanguagePreferenceCommonStrict languagePreference(String languageCode) {
        return LanguagePreferenceCommonStrict.builder()
            .languageCode(LanguagePreferenceCommonStrict.LanguageCodeEnum.fromValue(languageCode))
            .languageDisplayName("CY".equals(languageCode)
                ? LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.WELSH_AND_ENGLISH
                : LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.ENGLISH_ONLY)
            .build();
    }

    private void verifyNoAddSideEffects() {
        verifyNoInteractions(
            amendmentRepositoryService,
            aliasRepoService,
            debtorRepoService,
            defendantAccountPartiesRepository,
            partyRepositoryService
        );
    }
}
