package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;

@ExtendWith(MockitoExtension.class)
class DebtorDetailRepositoryServiceTest {

    @Mock
    private DebtorDetailRepository debtorDetailRepository;

    @InjectMocks
    private DebtorDetailRepositoryService service;

    // ─────────────────────────────────────────────────────────────────────────
    // addDebtorDetail
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void addDebtorDetail_savesNewEntity_withVehicleEmployerAndLanguage() {
        Long partyId = 42L;
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addDebtorDetail(
            partyId,
            vehicle("Toyota Corolla", "AB12 CDE"),
            employer("ACME Ltd", "EMP-001", "hr@acme.com", "01234567890",
                address("10 High St", null, "London", null, null, "EC1A 1BB")),
            languagePreferences("EN", "CY")
        );

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getPartyId()).isEqualTo(partyId);
        assertThat(saved.getVehicleMake()).isEqualTo("Toyota Corolla");
        assertThat(saved.getVehicleRegistration()).isEqualTo("AB12 CDE");
        assertThat(saved.getEmployerName()).isEqualTo("ACME Ltd");
        assertThat(saved.getEmployeeReference()).isEqualTo("EMP-001");
        assertThat(saved.getEmployerEmail()).isEqualTo("hr@acme.com");
        assertThat(saved.getEmployerTelephone()).isEqualTo("01234567890");
        assertThat(saved.getEmployerAddressLine1()).isEqualTo("10 High St");
        assertThat(saved.getEmployerAddressLine3()).isEqualTo("London");
        assertThat(saved.getEmployerPostcode()).isEqualTo("EC1A 1BB");
        assertThat(saved.getDocumentLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(saved.getHearingLanguage()).isEqualTo(Language.WELSH_AND_ENGLISH);
        assertThat(saved.getDocumentLanguageDate()).isNotNull();
        assertThat(saved.getHearingLanguageDate()).isNotNull();
    }

    @Test
    void addDebtorDetail_savesNewEntity_withNullVehicle() {
        Long partyId = 43L;
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addDebtorDetail(partyId, null, null, null);

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getPartyId()).isEqualTo(partyId);
        assertThat(saved.getVehicleMake()).isNull();
        assertThat(saved.getVehicleRegistration()).isNull();
        assertThat(saved.getEmployerName()).isNull();
        assertThat(saved.getDocumentLanguage()).isNull();
        assertThat(saved.getHearingLanguage()).isNull();
        assertThat(saved.getDocumentLanguageDate()).isNull();
        assertThat(saved.getHearingLanguageDate()).isNull();
    }

    @Test
    void addDebtorDetail_doesNotSave_whenPartyIdIsNull() {
        service.addDebtorDetail(null, vehicle("X", "Y"), null, null);

        verify(debtorDetailRepository, never()).save(any());
    }

    @Test
    void addDebtorDetail_savesEntity_withEmployerButNoAddress() {
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addDebtorDetail(
            99L,
            null,
            employer("Widgets Ltd", null, null, null, null),
            null
        );

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getEmployerName()).isEqualTo("Widgets Ltd");
        assertThat(saved.getEmployerAddressLine1()).isNull();
        assertThat(saved.getEmployerPostcode()).isNull();
    }

    @Test
    void addDebtorDetail_savesEntity_withEmployerAddress() {
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addDebtorDetail(
            100L,
            null,
            employer("Corp", "REF1", "corp@example.com", "0700000000",
                address("1 Corp Way", "Floor 2", "Manchester", "Greater Manchester", null, "M1 2AB")),
            null
        );

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getEmployerAddressLine1()).isEqualTo("1 Corp Way");
        assertThat(saved.getEmployerAddressLine2()).isEqualTo("Floor 2");
        assertThat(saved.getEmployerAddressLine3()).isEqualTo("Manchester");
        assertThat(saved.getEmployerAddressLine4()).isEqualTo("Greater Manchester");
        assertThat(saved.getEmployerAddressLine5()).isNull();
        assertThat(saved.getEmployerPostcode()).isEqualTo("M1 2AB");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateDebtorDetail
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void updateDebtorDetail_updatesExistingEntity_withAllFields() {
        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(55L);
        existing.setVehicleMake("Old Car");
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateDebtorDetail(
            existing,
            vehicle("New Car Model", "ZZ99 AAA"),
            employer("NewCo", "NEW-123", "new@co.com", "09876543210", null),
            languagePreferences("CY", "EN")
        );

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getPartyId()).isEqualTo(55L);
        assertThat(saved.getVehicleMake()).isEqualTo("New Car Model");
        assertThat(saved.getVehicleRegistration()).isEqualTo("ZZ99 AAA");
        assertThat(saved.getEmployerName()).isEqualTo("NewCo");
        assertThat(saved.getEmployeeReference()).isEqualTo("NEW-123");
        assertThat(saved.getEmployerEmail()).isEqualTo("new@co.com");
        assertThat(saved.getEmployerTelephone()).isEqualTo("09876543210");
        assertThat(saved.getDocumentLanguage()).isEqualTo(Language.WELSH_AND_ENGLISH);
        assertThat(saved.getHearingLanguage()).isEqualTo(Language.ENGLISH);
    }

    @Test
    void updateDebtorDetail_clearsVehicleFields_whenNullVehiclePassedAndPreservesLanguage() {
        // applyDebtorFields only sets language when non-null; it does not clear existing values on null input.
        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(66L);
        existing.setVehicleMake("Old Car");
        existing.setEmployerName("Old Employer");
        existing.setDocumentLanguage(Language.ENGLISH);
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateDebtorDetail(existing, null, null, null);

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getVehicleMake()).isNull();
        assertThat(saved.getVehicleRegistration()).isNull();
        // language is not cleared when null is passed — existing value is preserved
        assertThat(saved.getDocumentLanguage()).isEqualTo(Language.ENGLISH);
    }

    @Test
    void updateDebtorDetail_updatesEntity_withEmployerAndAddress() {
        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(77L);
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateDebtorDetail(
            existing,
            null,
            employer("Updated Corp", null, null, null,
                address("5 New St", null, "Bristol", null, null, "BS1 2EF")),
            null
        );

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        DebtorDetailEntity saved = captor.getValue();
        assertThat(saved.getEmployerName()).isEqualTo("Updated Corp");
        assertThat(saved.getEmployerAddressLine1()).isEqualTo("5 New St");
        assertThat(saved.getEmployerPostcode()).isEqualTo("BS1 2EF");
    }

    @Test
    void updateDebtorDetail_savesExistingEntityReference() {
        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(88L);
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateDebtorDetail(existing, null, null, null);

        ArgumentCaptor<DebtorDetailEntity> captor = ArgumentCaptor.forClass(DebtorDetailEntity.class);
        verify(debtorDetailRepository).save(captor.capture());

        assertThat(captor.getValue()).isSameAs(existing);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findById / findByPartyId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findById_returnsOptional_fromRepository() {
        DebtorDetailEntity entity = new DebtorDetailEntity();
        entity.setPartyId(1L);
        when(debtorDetailRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(service.findById(1L)).isPresent().contains(entity);
    }

    @Test
    void findByPartyId_returnsOptional_fromRepository() {
        DebtorDetailEntity entity = new DebtorDetailEntity();
        entity.setPartyId(2L);
        when(debtorDetailRepository.findByPartyId(2L)).thenReturn(Optional.of(entity));

        assertThat(service.findByPartyId(2L)).isPresent().contains(entity);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // builder helpers
    // ─────────────────────────────────────────────────────────────────────────

    private VehicleDetails vehicle(String makeAndModel, String registration) {
        return VehicleDetails.builder()
            .vehicleMakeAndModel(makeAndModel)
            .vehicleRegistration(registration)
            .build();
    }

    private EmployerDetails employer(String name, String ref, String email, String telephone,
        AddressDetails employerAddress) {
        return EmployerDetails.builder()
            .employerName(name)
            .employerReference(ref)
            .employerEmailAddress(email)
            .employerTelephoneNumber(telephone)
            .employerAddress(employerAddress)
            .build();
    }

    private AddressDetails address(String line1, String line2, String line3, String line4, String line5,
        String postcode) {
        return AddressDetails.builder()
            .addressLine1(line1)
            .addressLine2(line2)
            .addressLine3(line3)
            .addressLine4(line4)
            .addressLine5(line5)
            .postcode(postcode)
            .build();
    }

    private LanguagePreferences languagePreferences(String documentCode, String hearingCode) {
        return LanguagePreferences.builder()
            .documentLanguagePreference(LanguagePreference.fromCode(documentCode))
            .hearingLanguagePreference(LanguagePreference.fromCode(hearingCode))
            .build();
    }
}

