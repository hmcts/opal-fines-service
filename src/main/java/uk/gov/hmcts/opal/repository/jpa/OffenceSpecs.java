package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity_;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "opal.OffenceSpecs")
public class OffenceSpecs extends EntitySpecs<OffenceEntity.Lite> {

    public Specification<OffenceEntity.Lite> findBySearchCriteria(OffenceSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getOffenceId()).map(OffenceSpecs::equalsOffenceId),
            notBlank(criteria.getCjsCode()).map(OffenceSpecs::likeCjsCodeStartsWith),
            notNullOffsetDateTime(criteria.getActiveDate()).map(OffenceSpecs::usedFromDateLessThenEqualToDate),
            notNullOffsetDateTime(criteria.getActiveDate()).map(OffenceSpecs::usedToDateGreaterThenEqualToDate),
            notBlank(criteria.getTitle()).map(OffenceSpecs::likeEitherTitle),
            notBlank(criteria.getActSection()).map(OffenceSpecs::likeEitherOas)
        ));
    }

    public Specification<OffenceEntity.Lite> referenceDataFilter(Optional<String> filter,
                                                            Optional<Short> businessUnitId, List<String> cjsCode) {
        return Specification.allOf(specificationList(
            List.of(
                filter.filter(s -> !s.isBlank()).map(OffenceSpecs::likeAnyOffence),
                equalsAnyCjsCode(cjsCode)
            ),
            globalOrLocalOffence(businessUnitId)
        ));
    }

    public static Specification<OffenceEntity.Lite> equalsOffenceId(String offenceId) {
        return (root, query, builder) -> builder.equal(root.get(OffenceEntity_.offenceId), offenceId);
    }

    public static Specification<OffenceEntity.Lite> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            builder.equal(root.get(OffenceEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<OffenceEntity.Lite> likeCjsCode(String cjsCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenceEntity.Lite> likeCjsCodeStartsWith(String cjsCode) {
        return (root, query, builder) ->
            likeLowerCaseBothStartsWithPredicate(root.get(OffenceEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenceEntity.Lite> likeOffenceTitle(String offenceTitle) {
        log.debug(":likeOffenceTitle: like offence title: '{}'", offenceTitle);
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceTitle), builder, offenceTitle);
    }

    public static Specification<OffenceEntity.Lite> likeOffenceTitleCy(String offenceTitleCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceTitleCy), builder, offenceTitleCy);
    }

    public static Specification<OffenceEntity.Lite> likeOffenceOas(String actSection) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceOas), builder, actSection);
    }

    public static Specification<OffenceEntity.Lite> likeOffenceOasCy(String actSection) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceEntity_.offenceOasCy), builder, actSection);
    }

    public static Optional<Specification<OffenceEntity.Lite>> equalsAnyCjsCode(
        Collection<String> cjsCode) {

        if (cjsCode.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((root, query, builder) -> root.get(OffenceEntity_.cjsCode).in(cjsCode));
    }

    public static Specification<OffenceEntity.Lite> usedFromDateLessThenEqualToDate(
        LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceEntity_.dateUsedFrom)),
            builder.lessThanOrEqualTo(root.get(OffenceEntity_.dateUsedFrom), offenceStillValidDate)
        );
    }

    public static Specification<OffenceEntity.Lite> usedToDateGreaterThenEqualToDate(
        LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceEntity_.dateUsedTo)),
            builder.greaterThanOrEqualTo(root.get(OffenceEntity_.dateUsedTo), offenceStillValidDate)
        );
    }

    public static Predicate matchLocalOffencesPredicate(
        From<?, OffenceEntity.Lite> from, CriteriaBuilder builder, Short businessUnitId) {
        return businessUnitId == 0
            // when business unit equal to zero, return all 'local' offences (business unit not null)
            ? builder.isNotNull(from.get(OffenceEntity_.businessUnitId)) :
            // with business unit not equal to zero, return just 'local' offences for that business unit
            builder.equal(from.get(OffenceEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<OffenceEntity.Lite> likeAnyOffence(String filter) {
        return Specification.anyOf(
            likeCjsCode(filter),
            likeOffenceTitle(filter),
            likeOffenceTitleCy(filter)
        );
    }

    public static Specification<OffenceEntity.Lite> likeEitherTitle(String title) {
        return Specification.anyOf(
            likeOffenceTitle(title),
            likeOffenceTitleCy(title)
        );
    }

    public static Specification<OffenceEntity.Lite> likeEitherOas(String oas) {
        return Specification.anyOf(
            likeOffenceOas(oas),
            likeOffenceOasCy(oas)
        );
    }

    public static Specification<OffenceEntity.Lite> globalOrLocalOffence(Optional<Short> businessUnitId) {
        return (root, query, builder) ->
            businessUnitId
                // return 'local' offences, dependant upon business unit
                .map(bu -> matchLocalOffencesPredicate(root, builder, bu))
                // return all 'global' offences, defined as not having a business unit specified
                .orElse(builder.isNull(root.get(OffenceEntity_.businessUnitId)));
    }
}
