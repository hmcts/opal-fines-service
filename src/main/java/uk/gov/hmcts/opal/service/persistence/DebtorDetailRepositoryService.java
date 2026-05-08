package uk.gov.hmcts.opal.service.persistence;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
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
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language) {

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
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language) {

        log.debug("updateDebtorDetail: partyId: {}", debtor.getPartyId());

        applyDebtorFields(debtor, vehicle, employer, language);
        debtorDetailRepository.save(debtor);
    }

    private static void applyDebtorFields(DebtorDetailEntity debtor,
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language) {

        applyVehicleDetails(debtor, vehicle);
        applyEmployerDetails(debtor, employer);
        applyLanguagePreferences(debtor, language);
    }

    private static void applyVehicleDetails(DebtorDetailEntity debtor, VehicleDetails vehicle) {
        debtor.setVehicleMake(vehicle != null ? vehicle.getVehicleMakeAndModel() : null);
        debtor.setVehicleRegistration(vehicle != null ? vehicle.getVehicleRegistration() : null);
    }

    private static void applyEmployerDetails(DebtorDetailEntity debtor, EmployerDetails employer) {
        if (employer == null) {
            return;
        }

        debtor.setEmployerName(employer.getEmployerName());
        debtor.setEmployeeReference(employer.getEmployerReference());
        debtor.setEmployerEmail(employer.getEmployerEmailAddress());
        debtor.setEmployerTelephone(employer.getEmployerTelephoneNumber());

        applyEmployerAddress(debtor, employer.getEmployerAddress());
    }

    private static void applyEmployerAddress(DebtorDetailEntity debtor, AddressDetails address) {
        if (address == null) {
            return;
        }

        debtor.setEmployerAddressLine1(address.getAddressLine1());
        debtor.setEmployerAddressLine2(address.getAddressLine2());
        debtor.setEmployerAddressLine3(address.getAddressLine3());
        debtor.setEmployerAddressLine4(address.getAddressLine4());
        debtor.setEmployerAddressLine5(address.getAddressLine5());
        debtor.setEmployerPostcode(address.getPostcode());
    }

    private static void applyLanguagePreferences(DebtorDetailEntity debtor, LanguagePreferences language) {
        if (language == null) {
            return;
        }

        debtor.setDocumentLanguage(toLanguage(language.getDocumentLanguagePreference()));
        debtor.setHearingLanguage(toLanguage(language.getHearingLanguagePreference()));

        LocalDate now = LocalDate.now();
        debtor.setDocumentLanguageDate(now);
        debtor.setHearingLanguageDate(now);
    }

    private static Language toLanguage(LanguagePreference preference) {
        return preference != null ? Language.fromCode(preference.getLanguageCode()) : null;
    }
}
