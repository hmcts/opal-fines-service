package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity_;

public class DebtorDetailSpecs extends EntitySpecs<DebtorDetailEntity> {

    public Specification<DebtorDetailEntity> findBySearchCriteria(DebtorDetailSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPartyId()).map(DebtorDetailSpecs::equalsPartyId),
            notBlank(criteria.getEmail()).map(DebtorDetailSpecs::equalsEmail),
            notBlank(criteria.getVehicleMake()).map(DebtorDetailSpecs::equalsVehicleMake),
            notBlank(criteria.getVehicleRegistration()).map(DebtorDetailSpecs::equalsVehicleRegistration),
            notBlank(criteria.getEmployerName()).map(DebtorDetailSpecs::equalsEmployerName),
            notBlank(criteria.getEmployerAddressLine()).map(DebtorDetailSpecs::equalsEmployerAddressLine)
        ));
    }

    public static Specification<DebtorDetailEntity> equalsPartyId(String partyId) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.partyId), partyId);
    }

    public static Specification<DebtorDetailEntity> equalsEmail(String email) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.email1), email);
    }

    public static Specification<DebtorDetailEntity> equalsVehicleMake(String vehicleMake) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.vehicleMake), vehicleMake);
    }

    public static Specification<DebtorDetailEntity> equalsVehicleRegistration(String vehicleRegistration) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.vehicleRegistration),
                                                       vehicleRegistration);
    }

    public static Specification<DebtorDetailEntity> equalsEmployerName(String employerName) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.employerName), employerName);
    }

    public static Specification<DebtorDetailEntity> equalsEmployerAddressLine(String employerAddressLine) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.employerAddressLine1),
                                                       employerAddressLine);
    }

}
