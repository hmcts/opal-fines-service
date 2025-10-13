package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitLiteEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitLiteEntity_;

import java.util.Optional;

public class BusinessUnitLiteSpecs extends EntitySpecs<BusinessUnitLiteEntity> {

    public Specification<BusinessUnitLiteEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(BusinessUnitLiteSpecs::likeAnyBusinessUnit)
        ));
    }

    public static Specification<BusinessUnitLiteEntity> likeAnyBusinessUnit(String filter) {
        return Specification.anyOf(
            likeBusinessUnitName(filter),
            likeBusinessUnitCode(filter),
            likeBusinessUnitType(filter)
        );
    }

    public static Specification<BusinessUnitLiteEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitLiteEntity_.businessUnitName), builder, businessUnitName);
    }

    public static Specification<BusinessUnitLiteEntity> likeBusinessUnitCode(String businessUnitCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitLiteEntity_.businessUnitCode), builder, businessUnitCode);
    }

    public static Specification<BusinessUnitLiteEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(BusinessUnitLiteEntity_.businessUnitType), builder, businessUnitType);
    }
}
