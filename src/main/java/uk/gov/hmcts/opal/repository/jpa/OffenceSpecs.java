package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity_;

import java.time.LocalDateTime;
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

    public Specification<OffenceEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            Optional.of(LocalDateTime.now()).map(OffenceSpecs::usedToDateGreaterThenEqualToDate),
            filter.filter(s -> !s.isBlank()).map(OffenceSpecs::likeAnyOffence)
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

    public static Specification<OffenceEntity> usedToDateGreaterThenEqualToDate(LocalDateTime expiryDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceEntity_.dateUsedTo)),
            builder.greaterThanOrEqualTo(root.get(OffenceEntity_.dateUsedTo), expiryDate)
        );
    }

    public static Specification<OffenceEntity> likeAnyOffence(String filter) {
        return Specification.anyOf(
            likeCjsCode(filter),
            likeOffenceTitle(filter),
            likeOffenceTitleCy(filter)
        );
    }

    public static Join<OffenceEntity, BusinessUnitEntity> joinBusinessUnit(From<?, OffenceEntity> from) {
        return from.join(OffenceEntity_.businessUnit);
    }

}
