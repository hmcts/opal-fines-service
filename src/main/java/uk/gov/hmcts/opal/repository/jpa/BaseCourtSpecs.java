package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.BaseCourtSearch;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.EnforcerCourtBaseEntity;
import uk.gov.hmcts.opal.entity.EnforcerCourtBaseEntity_;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public abstract class BaseCourtSpecs<E extends EnforcerCourtBaseEntity> extends AddressSpecs<E> {

    @SuppressWarnings("unchecked")
    public List<Optional<Specification<E>>> findByBaseCourtCriteria(BaseCourtSearch criteria) {
        return combine(findByAddressCriteria(criteria),
                numericShort(criteria.getBusinessUnitId()).map(this::equalsBusinessUnitId),
                notBlank(criteria.getNameCy()).map(this::likeNameCy),
                notBlank(criteria.getAddressLineCy()).map(this::likeAnyAddressLineCy));
    }


    public Specification<E> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }


    public Specification<E> likeNameCy(String nameCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(EnforcerCourtBaseEntity_.nameCy), builder, nameCy);
    }

    public Specification<E> likeAnyAddressLineCy(String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return Specification.anyOf(
            likeAddressLine1Cy(addressLinePattern),
            likeAddressLine2Cy(addressLinePattern),
            likeAddressLine3Cy(addressLinePattern));
    }

    private Specification<E> likeAddressLine1Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCasePredicate(root.get(EnforcerCourtBaseEntity_.addressLine1Cy), builder, addressLinePattern);
    }

    private Specification<E> likeAddressLine2Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCasePredicate(root.get(EnforcerCourtBaseEntity_.addressLine2Cy), builder, addressLinePattern);
    }

    private Specification<E> likeAddressLine3Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCasePredicate(root.get(EnforcerCourtBaseEntity_.addressLine3Cy), builder, addressLinePattern);
    }

    public Join<E, BusinessUnitEntity> joinBusinessUnit(From<?, E> from) {
        return from.join(EnforcerCourtBaseEntity_.businessUnit);
    }

}
