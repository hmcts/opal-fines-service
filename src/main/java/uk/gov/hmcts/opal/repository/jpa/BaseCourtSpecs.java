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
                notBlank(criteria.getNameCy()).map(this::likeNameCy),
                notBlank(criteria.getAddressLineCy()).map(this::likeAnyAddressLineCy));
    }


    public Specification<E> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerCourtBaseEntity_.businessUnitId),
                                                       businessUnitId);
    }

    public Specification<E> likeNameCy(String nameCy) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(EnforcerCourtBaseEntity_.nameCy)),
                                                       "%" + nameCy.toLowerCase() + "%");
    }

    public Specification<E> likeAnyAddressLineCy(String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return Specification.anyOf(
            likeAddressLine1Cy(addressLinePattern),
            likeAddressLine2Cy(addressLinePattern),
            likeAddressLine3Cy(addressLinePattern));
    }

    private Specification<E> likeAddressLine1Cy(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(EnforcerCourtBaseEntity_.addressLine1Cy)),
                                                      addressLinePattern);
    }

    private Specification<E> likeAddressLine2Cy(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(EnforcerCourtBaseEntity_.addressLine2Cy)),
                                                      addressLinePattern);
    }

    private Specification<E> likeAddressLine3Cy(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(EnforcerCourtBaseEntity_.addressLine3Cy)),
                                                      addressLinePattern);
    }

}
