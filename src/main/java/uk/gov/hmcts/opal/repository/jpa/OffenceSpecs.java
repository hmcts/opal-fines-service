package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceLegacyEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceLegacyEntity_;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "opal.OffenceSpecs")
public class OffenceSpecs extends EntitySpecs<OffenceLegacyEntity> {

    public Specification<OffenceLegacyEntity> findBySearchCriteria(OffenceSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getOffenceId()).map(OffenceSpecs::equalsOffenceId),
            notBlank(criteria.getCjsCode()).map(OffenceSpecs::likeCjsCodeStartsWith),
            notNullOffsetDateTime(criteria.getActiveDate()).map(OffenceSpecs::usedFromDateLessThenEqualToDate),
            notNullOffsetDateTime(criteria.getActiveDate()).map(OffenceSpecs::usedToDateGreaterThenEqualToDate),
            notBlank(criteria.getTitle()).map(OffenceSpecs::likeEitherTitle),
            notBlank(criteria.getActSection()).map(OffenceSpecs::likeEitherOas)
        ));
    }

    public Specification<OffenceLegacyEntity> referenceDataFilter(Optional<String> filter,
                                                            Optional<Short> businessUnitId, List<String> cjsCode) {
        return Specification.allOf(specificationList(
            List.of(
                filter.filter(s -> !s.isBlank()).map(OffenceSpecs::likeAnyOffence),
                equalsAnyCjsCode(cjsCode)
            ),
            globalOrLocalOffence(businessUnitId)
        ));
    }

    public static Specification<OffenceLegacyEntity> equalsOffenceId(String offenceId) {
        return (root, query, builder) -> builder.equal(root.get(OffenceLegacyEntity_.offenceId), offenceId);
    }

    public static Specification<OffenceLegacyEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            builder.equal(root.get(OffenceLegacyEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<OffenceLegacyEntity> likeCjsCode(String cjsCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceLegacyEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenceLegacyEntity> likeCjsCodeStartsWith(String cjsCode) {
        return (root, query, builder) ->
            likeLowerCaseBothStartsWithPredicate(root.get(OffenceLegacyEntity_.cjsCode), builder, cjsCode);
    }

    public static Specification<OffenceLegacyEntity> likeOffenceTitle(String offenceTitle) {
        log.debug(":likeOffenceTitle: like offence title: '{}'", offenceTitle);
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceLegacyEntity_.offenceTitle), builder, offenceTitle);
    }

    public static Specification<OffenceLegacyEntity> likeOffenceTitleCy(String offenceTitleCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceLegacyEntity_.offenceTitleCy), builder, offenceTitleCy);
    }

    public static Specification<OffenceLegacyEntity> likeOffenceOas(String actSection) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceLegacyEntity_.offenceOas), builder, actSection);
    }

    public static Specification<OffenceLegacyEntity> likeOffenceOasCy(String actSection) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(OffenceLegacyEntity_.offenceOasCy), builder, actSection);
    }

    public static Optional<Specification<OffenceLegacyEntity>> equalsAnyCjsCode(
        Collection<String> cjsCode) {

        if (cjsCode.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((root, query, builder) -> root.get(OffenceLegacyEntity_.cjsCode).in(cjsCode));
    }

    public static Specification<OffenceLegacyEntity> usedFromDateLessThenEqualToDate(
        LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceLegacyEntity_.dateUsedFrom)),
            builder.lessThanOrEqualTo(root.get(OffenceLegacyEntity_.dateUsedFrom), offenceStillValidDate)
        );
    }

    public static Specification<OffenceLegacyEntity> usedToDateGreaterThenEqualToDate(
        LocalDateTime offenceStillValidDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(OffenceLegacyEntity_.dateUsedTo)),
            builder.greaterThanOrEqualTo(root.get(OffenceLegacyEntity_.dateUsedTo), offenceStillValidDate)
        );
    }

    public static Predicate matchLocalOffencesPredicate(
        From<?, OffenceLegacyEntity> from, CriteriaBuilder builder, Short businessUnitId) {
        return businessUnitId == 0
            // when business unit equal to zero, return all 'local' offences (business unit not null)
            ? builder.isNotNull(from.get(OffenceLegacyEntity_.businessUnitId)) :
            // with business unit not equal to zero, return just 'local' offences for that business unit
            builder.equal(from.get(OffenceLegacyEntity_.businessUnitId), businessUnitId);
    }

    public static Specification<OffenceLegacyEntity> likeAnyOffence(String filter) {
        return Specification.anyOf(
            likeCjsCode(filter),
            likeOffenceTitle(filter),
            likeOffenceTitleCy(filter)
        );
    }

    public static Specification<OffenceLegacyEntity> likeEitherTitle(String title) {
        return Specification.anyOf(
            likeOffenceTitle(title),
            likeOffenceTitleCy(title)
        );
    }

    public static Specification<OffenceLegacyEntity> likeEitherOas(String oas) {
        return Specification.anyOf(
            likeOffenceOas(oas),
            likeOffenceOasCy(oas)
        );
    }

    public static Specification<OffenceLegacyEntity> globalOrLocalOffence(Optional<Short> businessUnitId) {
        return (root, query, builder) ->
            businessUnitId
                // return 'local' offences, dependant upon business unit
                .map(bu -> matchLocalOffencesPredicate(root, builder, bu))
                // return all 'global' offences, defined as not having a business unit specified
                .orElse(builder.isNull(root.get(OffenceLegacyEntity_.businessUnitId)));
    }
}
