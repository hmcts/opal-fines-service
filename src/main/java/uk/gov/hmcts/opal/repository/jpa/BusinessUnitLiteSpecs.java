package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntityLite;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntityLite_;

import java.util.Optional;

public class BusinessUnitLiteSpecs extends EntitySpecs<BusinessUnitEntityLite> {

    public Specification<BusinessUnitEntityLite> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitLiteSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnitEntityLite> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }

    public static Specification<BusinessUnitEntityLite> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitEntityLite_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitEntityLite> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitEntityLite_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitEntityLite> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitEntityLite_.businessUnitType), builder, businessUnitType);
    }
}
