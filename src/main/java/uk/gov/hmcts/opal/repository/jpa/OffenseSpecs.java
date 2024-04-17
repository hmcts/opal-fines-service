package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.entity.OffenseEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;

public class OffenseSpecs extends EntitySpecs<OffenseEntity> {

    public Specification<OffenseEntity> findBySearchCriteria(OffenseSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getOffenseId()).map(OffenseSpecs::equalsOffenseId),
            notBlank(criteria.getCjsCode()).map(OffenseSpecs::likeCjsCode),
            numericShort(criteria.getBusinessUnitId()).map(OffenseSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(OffenseSpecs::likeBusinessUnitName),
            notBlank(criteria.getOffenceTitle()).map(OffenseSpecs::likeOffenseTitle),
            notBlank(criteria.getOffenceTitleCy()).map(OffenseSpecs::likeOffenseTitleCy)
        ));
    }

    public static Specification<OffenseEntity> equalsOffenseId(String offenseId) {
        return (root, query, builder) -> builder.equal(root.get(OffenseEntity_.offenseId), offenseId);
    }

    public static Specification<OffenseEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<OffenseEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<OffenseEntity> likeCjsCode(String cjsCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenseEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenseEntity> likeOffenseTitle(String offenseTitle) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenseEntity_.offenseTitle), builder, offenseTitle);
    }

    public static Specification<OffenseEntity> likeOffenseTitleCy(String offenseTitleCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenseEntity_.offenseTitleCy), builder, offenseTitleCy);
    }

    public static Join<OffenseEntity, BusinessUnitEntity> joinBusinessUnit(From<?, OffenseEntity> from) {
        return from.join(OffenseEntity_.businessUnit);
    }
}
