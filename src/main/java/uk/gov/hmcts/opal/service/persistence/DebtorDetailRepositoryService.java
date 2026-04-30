package uk.gov.hmcts.opal.service.persistence;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
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

    private void applyDebtorFields(DebtorDetailEntity debtor,
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language) {

        debtor.setVehicleMake(vehicle != null ? vehicle.getVehicleMakeAndModel() : null);
        debtor.setVehicleRegistration(vehicle != null ? vehicle.getVehicleRegistration() : null);

        if (employer != null) {
            debtor.setEmployerName(employer.getEmployerName());
            debtor.setEmployeeReference(employer.getEmployerReference());
            debtor.setEmployerEmail(employer.getEmployerEmailAddress());
            debtor.setEmployerTelephone(employer.getEmployerTelephoneNumber());

            AddressDetails ea = employer.getEmployerAddress();
            if (ea != null) {
                debtor.setEmployerAddressLine1(ea.getAddressLine1());
                debtor.setEmployerAddressLine2(ea.getAddressLine2());
                debtor.setEmployerAddressLine3(ea.getAddressLine3());
                debtor.setEmployerAddressLine4(ea.getAddressLine4());
                debtor.setEmployerAddressLine5(ea.getAddressLine5());
                debtor.setEmployerPostcode(ea.getPostcode());
            }
        }

        if (language != null) {
            debtor.setDocumentLanguage(language.getDocumentLanguagePreference() != null
                ? Language.fromCode(language.getDocumentLanguagePreference().getLanguageCode()) : null);
            debtor.setHearingLanguage(language.getHearingLanguagePreference() != null
                ? Language.fromCode(language.getHearingLanguagePreference().getLanguageCode()) : null);
            debtor.setDocumentLanguageDate(LocalDate.now());
            debtor.setHearingLanguageDate(LocalDate.now());
        }
    }
}
