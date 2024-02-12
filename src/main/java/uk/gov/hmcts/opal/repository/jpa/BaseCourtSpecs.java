package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BaseCourtSearch;
import uk.gov.hmcts.opal.entity.EnforcerCourtBaseEntity;
import uk.gov.hmcts.opal.entity.EnforcerCourtBaseEntity_;

import java.util.List;
import java.util.Optional;

public abstract class BaseCourtSpecs<E extends EnforcerCourtBaseEntity> extends AddressSpecs<E> {

    @SuppressWarnings("unchecked")
    public List<Optional<Specification<E>>> findByBaseCourtCriteria(BaseCourtSearch criteria) {
        return combine(findByAddressCriteria(criteria),
                notBlank(criteria.getBusinessUnitId()).map(this::equalsBusinessUnitId),
                notBlank(criteria.getNameCy()).map(this::equalsNameCy),
                notBlank(criteria.getAddressLineCy()).map(this::equalsAddressLineCy));
    }


    public Specification<E> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerCourtBaseEntity_.businessUnitId),
                                                       businessUnitId);
    }

    public Specification<E> equalsNameCy(String nameCy) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerCourtBaseEntity_.nameCy), nameCy);
    }

    public Specification<E> equalsAddressLineCy(String addressLineCy) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerCourtBaseEntity_.addressLine1Cy),
                                                       addressLineCy);
    }

}
