package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity_;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsAnyBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class DraftAccountSpecs extends EntitySpecs<DraftAccountEntity> {

    public Specification<DraftAccountEntity> findBySearchCriteria(DraftAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getDraftAccountId()).map(DraftAccountSpecs::equalsDraftAccountId),
            numericShort(criteria.getBusinessUnitId()).map(DraftAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getAccountType()).map(DraftAccountSpecs::likeAccountType),
            notBlank(criteria.getAccountStatus()).map(DraftAccountSpecs::equalsAccountStatus)
        ));
    }

    public Specification<DraftAccountEntity> findForSummaries(
        Collection<Short> businessUnitIds, Collection<DraftAccountStatus> statuses,
        Collection<String> submittedBys, Collection<String> notSubmitted,
        Optional<LocalDate> accountStatusDateFrom, Optional<LocalDate> accountStatusDateTo) {

        return Specification.allOf(specificationList(
            equalsAnyBusinessUnitId(businessUnitIds),
            equalsAnyAccountStatus(statuses),
            equalsAnySubmittedBy(submittedBys),
            equalsNotSubmittedBy(notSubmitted),
            accountStatusDateUpTo(accountStatusDateTo.map(toDate -> toDate.plusDays(1).atStartOfDay())),
            accountStatusDateFrom(accountStatusDateFrom.map(LocalDate::atStartOfDay))
        ));
    }

    public static Specification<DraftAccountEntity> equalsDraftAccountId(Long draftAccountId) {
        return (root, query, builder) -> builder.equal(root.get(DraftAccountEntity_.draftAccountId), draftAccountId);
    }

    public static Specification<DraftAccountEntity> equalsSubmittedBy(String submittedBy) {
        return (root, query, builder) -> builder.equal(root.get(DraftAccountEntity_.submittedBy), submittedBy);
    }

    public static Optional<Specification<DraftAccountEntity>> equalsAnySubmittedBy(Collection<String> submittedBys) {

        if (submittedBys.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of((root, query, builder) -> root.get(DraftAccountEntity_.submittedBy).in(submittedBys));
    }

    public static Optional<Specification<DraftAccountEntity>> equalsNotSubmittedBy(Collection<String> notSubmittedBy) {

        if (notSubmittedBy.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of((root, query, builder) ->
                               root.get(DraftAccountEntity_.submittedBy).in(notSubmittedBy).not());
    }

    public static Specification<DraftAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Optional<Specification<DraftAccountEntity>> equalsAnyBusinessUnitId(
        Collection<Short> businessUnitId) {

        if (businessUnitId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((root, query, builder) ->
            equalsAnyBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId));
    }

    public static Specification<DraftAccountEntity> likeAccountType(String accountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DraftAccountEntity_.accountType), builder, accountType);
    }

    public static Specification<DraftAccountEntity> equalsAccountStatus(String accountStatus) {
        DraftAccountStatus status = DraftAccountStatus.valueOf(accountStatus);
        return (root, query, builder) -> builder.equal(root.get(DraftAccountEntity_.accountStatus), status);
    }

    public static Optional<Specification<DraftAccountEntity>> equalsAnyAccountStatus(
        Collection<DraftAccountStatus> accountStatuses) {

        if (accountStatuses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((root, query, builder) -> root.get(DraftAccountEntity_.accountStatus).in(accountStatuses));
    }

    public static Optional<Specification<DraftAccountEntity>> accountStatusDateUpTo(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(DraftAccountEntity_.accountStatusDate)),
            builder.lessThan(root.get(DraftAccountEntity_.accountStatusDate), dateTime)
        )));
    }

    public static Optional<Specification<DraftAccountEntity>> accountStatusDateFrom(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(DraftAccountEntity_.accountStatusDate)),
            builder.greaterThanOrEqualTo(root.get(DraftAccountEntity_.accountStatusDate), dateTime)
        )));
    }

    public static Join<DraftAccountEntity, BusinessUnitEntity> joinBusinessUnit(From<?, DraftAccountEntity> from) {
        return from.join(DraftAccountEntity_.businessUnit);
    }
}
