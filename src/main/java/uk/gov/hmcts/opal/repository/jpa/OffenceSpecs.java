package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity_;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;

public class OffenceSpecs extends EntitySpecs<OffenceEntity> {

    public Specification<OffenceEntity> findBySearchCriteria(OffenceSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getOffenceId()).map(OffenceSpecs::equalsOffenceId),
            notBlank(criteria.getCjsCode()).map(OffenceSpecs::likeCjsCode),
            numericShort(criteria.getBusinessUnitId()).map(OffenceSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(OffenceSpecs::likeBusinessUnitName),
            notBlank(criteria.getOffenceTitle()).map(OffenceSpecs::likeOffenceTitle),
            notBlank(criteria.getOffenceTitleCy()).map(OffenceSpecs::likeOffenceTitleCy)
        ));
    }

    public Specification<OffenceEntity> referenceDataFilter(Optional<String> filter,
                                                            Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            List.of(
                filter.filter(s -> !s.isBlank()).map(OffenceSpecs::likeAnyOffence)),
            globalOrLocalOffence(businessUnitId)
        ));
    }

    public static Specification<OffenceEntity> equalsOffenceId(String offenceId) {
        return (root, query, builder) -> builder.equal(root.get(OffenceEntity_.offenceId), offenceId);
    }

    public static Specification<OffenceEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<OffenceEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<OffenceEntity> likeCjsCode(String cjsCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenceEntity> likeOffenceTitle(String offenceTitle) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceTitle), builder, offenceTitle);
    }

    public static Specification<OffenceEntity> likeOffenceTitleCy(String offenceTitleCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceTitleCy), builder, offenceTitleCy);
    }

    public static Specification<OffenceEntity> usedFromDateLessThenEqualToDate(LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceEntity_.dateUsedFrom)),
            builder.lessThanOrEqualTo(root.get(OffenceEntity_.dateUsedFrom), offenceStillValidDate)
        );
    }

    public static Specification<OffenceEntity> usedToDateGreaterThenEqualToDate(LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceEntity_.dateUsedTo)),
            builder.greaterThanOrEqualTo(root.get(OffenceEntity_.dateUsedTo), offenceStillValidDate)
        );
    }

    public static Predicate matchLocalOffencesPredicate(
        From<?, OffenceEntity> from, CriteriaBuilder builder, Short businessUnitId) {
        return businessUnitId == 0
            // when business unit equal to zero, return all 'local' offences (business unit not null)
            ? builder.isNotNull(from.get(OffenceEntity_.businessUnit)) :
            // with business unit not equal to zero, return just 'local' offences for that business unit
            equalsBusinessUnitIdPredicate(joinBusinessUnit(from), builder, businessUnitId);
    }

    public static Specification<OffenceEntity> likeAnyOffence(String filter) {
        return Specification.anyOf(
            likeCjsCode(filter),
            likeOffenceTitle(filter),
            likeOffenceTitleCy(filter)
        );
    }

    public static Specification<OffenceEntity> globalOrLocalOffence(Optional<Short> businessUnitId) {
        return (root, query, builder) ->
            businessUnitId
                // return 'local' offences, dependant upon business unit
                .map(bu -> matchLocalOffencesPredicate(root, builder, bu))
                // return all 'global' offences, defined as not having a business unit specified
                .orElse(builder.isNull(root.get(OffenceEntity_.businessUnit)));
    }

    public static Join<OffenceEntity, BusinessUnitEntity> joinBusinessUnit(From<?, OffenceEntity> from) {
        return from.join(OffenceEntity_.businessUnit);
    }

}
