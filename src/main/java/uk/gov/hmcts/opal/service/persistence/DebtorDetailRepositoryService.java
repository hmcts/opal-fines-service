package uk.gov.hmcts.opal.service.persistence;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;

@Service
@Slf4j(topic = "opal.DebtorDetailRepositoryService")
@RequiredArgsConstructor
public class DebtorDetailRepositoryService {

    private final DebtorDetailRepository debtorDetailRepository;

    @Transactional(readOnly = true)
    public Optional<DebtorDetailEntity> findById(Long id) {
        return debtorDetailRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DebtorDetailEntity> findByPartyId(Long partyId) {
        return debtorDetailRepository.findByPartyId(partyId);
    }

    @Transactional
    public DebtorDetailEntity save(DebtorDetailEntity debtorDetailEntity) {
        return debtorDetailRepository.save(debtorDetailEntity);
    }

    @Transactional
    public void addDebtorDetail(Long partyId,
        PartyVehicleDetailsDefendantAccount vehicle,
        PartyEmployerDetailsDefendantAccount employer,
        LanguagePreferencesCommonStrict language) {

        log.debug("addDebtorDetail: partyId: {}", partyId);

        if (partyId == null) {
            return;
        }

        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setPartyId(partyId);
        applyDebtorFields(debtor, vehicle, employer, language);
        debtorDetailRepository.save(debtor);
    }

    @Transactional
    public void updateDebtorDetail(DebtorDetailEntity debtor,
        PartyVehicleDetailsDefendantAccount vehicle,
        PartyEmployerDetailsDefendantAccount employer,
        LanguagePreferencesCommonStrict language) {

        log.debug("updateDebtorDetail: partyId: {}", debtor.getPartyId());

        applyDebtorFields(debtor, vehicle, employer, language);
        debtorDetailRepository.save(debtor);
    }

    private static void applyDebtorFields(DebtorDetailEntity debtor,
        PartyVehicleDetailsDefendantAccount vehicle,
        PartyEmployerDetailsDefendantAccount employer,
        LanguagePreferencesCommonStrict language) {

        applyVehicleDetails(debtor, vehicle);
        applyEmployerDetails(debtor, employer);
        applyLanguagePreferences(debtor, language);
    }

    private static void applyVehicleDetails(DebtorDetailEntity debtor,
                                            PartyVehicleDetailsDefendantAccount vehicle) {
        debtor.setVehicleMake(vehicle != null ? value(vehicle.getVehicleMakeAndModel()) : null);
        debtor.setVehicleRegistration(vehicle != null ? value(vehicle.getVehicleRegistration()) : null);
    }

    private static void applyEmployerDetails(DebtorDetailEntity debtor,
                                             PartyEmployerDetailsDefendantAccount employer) {
        if (employer == null) {
            return;
        }

        debtor.setEmployerName(value(employer.getEmployerName()));
        debtor.setEmployeeReference(value(employer.getEmployerReference()));
        debtor.setEmployerEmail(value(employer.getEmployerEmailAddress()));
        debtor.setEmployerTelephone(value(employer.getEmployerTelephoneNumber()));

        applyEmployerAddress(debtor, employer.getEmployerAddress());
    }

    private static void applyEmployerAddress(DebtorDetailEntity debtor, AddressDetailsCommonStrict address) {
        if (address == null) {
            return;
        }

        debtor.setEmployerAddressLine1(address.getAddressLine1());
        debtor.setEmployerAddressLine2(value(address.getAddressLine2()));
        debtor.setEmployerAddressLine3(value(address.getAddressLine3()));
        debtor.setEmployerAddressLine4(value(address.getAddressLine4()));
        debtor.setEmployerAddressLine5(value(address.getAddressLine5()));
        debtor.setEmployerPostcode(value(address.getPostcode()));
    }

    private static void applyLanguagePreferences(DebtorDetailEntity debtor, LanguagePreferencesCommonStrict language) {
        if (language == null) {
            return;
        }

        debtor.setDocumentLanguage(toLanguage(value(language.getDocumentLanguagePreference())));
        debtor.setHearingLanguage(toLanguage(value(language.getHearingLanguagePreference())));

        LocalDate now = LocalDate.now();
        debtor.setDocumentLanguageDate(now);
        debtor.setHearingLanguageDate(now);
    }

    private static Language toLanguage(LanguagePreferenceCommonStrict preference) {
        return preference != null ? Language.fromCode(preference.getLanguageCode().getValue()) : null;
    }

    private static <T> T value(JsonNullable<T> nullable) {
        return nullable == null ? null : nullable.orElse(null);
    }
}
