package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.CLOSED;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.LIVE;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITH;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITHOUT;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;
import static uk.gov.hmcts.opal.repository.jpa.AliasSpecs.aliasPartyPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsAnyBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.equalsCourtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartySpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.addressLine1StartsWithPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.dateOfBirthStartsWithPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsDateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeOrganisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.namePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.niNumberStartsWithPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.postcodeStartsWithPredicate;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;
import static uk.gov.hmcts.opal.util.AgeUtil.ADULT_AGE;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.AccountStatusReportFilterType;
import uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportFiltersDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_;

@Component
public class DefendantAccountSpecs extends EntitySpecs<DefendantAccountEntity> {

    public static final AssociationType DEFENDANT_ASSOC_TYPE = AssociationType.DEFENDANT;

    public Specification<DefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getSurname)
                .map(DefendantAccountSpecs::likeSurname),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getForenames)
                .map(DefendantAccountSpecs::likeForename),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getBirthDate)
                .map(DefendantAccountSpecs::equalsDateOfBirth),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getNationalInsuranceNumber)
                .map(DefendantAccountSpecs::likeNiNumber),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getAddressLine1)
                .map(DefendantAccountSpecs::likeAnyAddressLine),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getPostcode)
                .map(DefendantAccountSpecs::likePostcode)
        ));
    }

    public static Predicate equalsDefendantAccountIdPredicate(
        From<?, DefendantAccountEntity> from, CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantAccountEntity_.defendantAccountId), defendantAccountId);
    }

    public static Specification<DefendantAccountEntity> idsIn(List<Long> defendantAccountIds) {
        return (root, query, builder) -> defendantAccountIds == null || defendantAccountIds.isEmpty()
            ? builder.disjunction()
            : defendantAccountIdPath(root).in(defendantAccountIds);
    }

    public static Path<Long> defendantAccountIdPath(From<?, DefendantAccountEntity> from) {
        return from.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID);
    }

    public static Predicate businessUnitIdsInPredicate(From<?, DefendantAccountEntity> from,
        Collection<Long> businessUnitIds) {

        return from.get(DefendantAccountEntity_.BUSINESS_UNIT)
            .get(SearchDefendantAccount_.BUSINESS_UNIT_ID)
            .in(businessUnitIds);
    }

    public static Predicate operationReportAccountTypesPredicate(From<?, DefendantAccountEntity> from,
        CriteriaBuilder builder, OperationReportFiltersDto filters) {

        Join<?, ?> link = from.join(DefendantAccountEntity_.PARTIES, JoinType.LEFT);
        Join<?, ?> party = link.join(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);

        return builder.or(optionalPredicateArray(
            selected(filters.getIncludeAdult()).map(ignore -> adultPartyPredicate(party, builder)),
            selected(filters.getIncludeYouth()).map(ignore -> youthPartyPredicate(party, builder)),
            selected(filters.getIncludeCompany()).map(ignore -> companyPartyPredicate(party, builder))
        ));
    }

    public static Predicate adultPartyPredicate(From<?, ?> party, CriteriaBuilder builder) {
        return builder.greaterThanOrEqualTo(party.get(PartyEntity_.AGE), ADULT_AGE);
    }

    public static Predicate youthPartyPredicate(From<?, ?> party, CriteriaBuilder builder) {
        return builder.lessThan(party.get(PartyEntity_.AGE), ADULT_AGE);
    }

    public static Predicate companyPartyPredicate(From<?, ?> party, CriteriaBuilder builder) {
        return builder.isTrue(party.get(PartyEntity_.ORGANISATION));
    }

    public static Predicate parentGuardianExistsPredicate(From<?, DefendantAccountEntity> from,
        CriteriaQuery<?> query, CriteriaBuilder builder) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<DefendantAccountPartiesEntity> dap = subquery.from(DefendantAccountPartiesEntity.class);
        subquery.select(builder.literal(1L));
        subquery.where(
            builder.equal(
                dap.get(DefendantAccountPartiesEntity_.DEFENDANT_ACCOUNT)
                    .get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID),
                defendantAccountIdPath(from)),
            builder.equal(dap.get(DefendantAccountPartiesEntity_.ASSOCIATION_TYPE), PARENT_GUARDIAN));
        return builder.exists(subquery);
    }

    public static Predicate collectionOrderPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        CollectionOrderReportFilterType choice) {

        if (WITH.equals(choice)) {
            return builder.isTrue(from.get(DefendantAccountEntity_.COLLECTION_ORDER));
        }
        if (WITHOUT.equals(choice)) {
            return builder.isFalse(from.get(DefendantAccountEntity_.COLLECTION_ORDER));
        }
        return builder.conjunction();
    }

    public static Predicate accountStatusPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        AccountStatusReportFilterType status) {

        if (LIVE.equals(status)) {
            return liveAccountPredicate(from, builder);
        }
        if (CLOSED.equals(status)) {
            return closedAccountPredicate(from, builder);
        }
        return builder.conjunction();
    }

    public static Predicate liveAccountPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder) {
        return builder.and(
            builder.greaterThan(from.get(DefendantAccountEntity_.ACCOUNT_BALANCE), builder.literal(BigDecimal.ZERO)),
            builder.isNull(from.get(DefendantAccountEntity_.COMPLETED_DATE)));
    }

    public static Predicate closedAccountPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder) {
        return builder.or(
            builder.equal(from.get(DefendantAccountEntity_.ACCOUNT_BALANCE), builder.literal(BigDecimal.ZERO)),
            builder.isNotNull(from.get(DefendantAccountEntity_.COMPLETED_DATE)));
    }

    public static Predicate minBalancePredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        BigDecimal minBalance) {

        return builder.greaterThanOrEqualTo(from.get(DefendantAccountEntity_.ACCOUNT_BALANCE), minBalance);
    }

    public static Predicate maxBalancePredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        BigDecimal maxBalance) {

        return builder.lessThanOrEqualTo(from.get(DefendantAccountEntity_.ACCOUNT_BALANCE), maxBalance);
    }

    public static Predicate nameRangePredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        String lowerNameRange, String upperNameRange) {

        Join<?, ?> link = from.join(DefendantAccountEntity_.PARTIES, JoinType.LEFT);
        Join<?, ?> party = link.join(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);
        Expression<String> firstLetter = nameFirstLetterPath(party, builder);

        return builder.and(optionalPredicateArray(
            notBlankValue(lowerNameRange)
                .map(value -> builder.greaterThanOrEqualTo(firstLetter, value.toLowerCase())),
            notBlankValue(upperNameRange)
                .map(value -> builder.lessThanOrEqualTo(firstLetter, value.toLowerCase()))
        ));
    }

    public static Expression<String> nameFirstLetterPath(From<?, ?> party, CriteriaBuilder builder) {
        return builder.lower(
            builder.substring(
                builder.coalesce(party.get(PartyEntity_.SURNAME), party.get(PartyEntity_.ORGANISATION_NAME)),
                1, 1));
    }

    public static Predicate notUnderEnforcementPredicate(From<?, DefendantAccountEntity> from,
        CriteriaBuilder builder) {

        return builder.isNull(from.get(DefendantAccountEntity_.LAST_ENFORCEMENT));
    }

    public static Predicate lastEnforcementPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        ResultId lastEnforcementFilter) {

        return builder.equal(from.get(DefendantAccountEntity_.LAST_ENFORCEMENT), lastEnforcementFilter.value());
    }

    public static Specification<DefendantAccountEntity> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccountEntity_.accountNumber), accountNo);
    }

    public static Specification<DefendantAccountEntity> equalsAnyCourtId(Long courtId) {
        return Specification.anyOf(
            equalsImposingCourtId(courtId),
            equalsEnforcingCourtId(courtId),
            equalsLastHearingCourtId(courtId));
    }

    public static Specification<DefendantAccountEntity> equalsImposingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccountEntity_.imposingCourtId), courtId);
    }

    public static Specification<DefendantAccountEntity> equalsEnforcingCourtId(Long courtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(joinEnforcingCourt(root), builder, courtId);
    }

    public static Specification<DefendantAccountEntity> equalsLastHearingCourtId(Long courtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(joinLastHearingCourt(root), builder, courtId);
    }

    public static Specification<DefendantAccountEntity> likeSurname(String surname) {
        return (root, query, builder) ->
            likeSurnamePredicate(joinDefendantParty(root, builder), builder, surname);
    }

    public static Specification<DefendantAccountEntity> likeForename(String forename) {
        return (root, query, builder) ->
            likeForenamesPredicate(joinDefendantParty(root, builder), builder, forename);
    }

    public static Specification<DefendantAccountEntity> likeOrganisationName(String organisation) {
        return (root, query, builder) ->
            likeOrganisationNamePredicate(joinDefendantParty(root, builder), builder, organisation);
    }

    public static Specification<DefendantAccountEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) ->
            equalsDateOfBirthPredicate(joinDefendantParty(root, builder), builder, dob);
    }

    public static Specification<DefendantAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) ->
            likeNiNumberPredicate(joinDefendantParty(root, builder), builder, niNumber);
    }

    public static Specification<DefendantAccountEntity> likeAnyAddressLine(String addressLine) {
        return (root, query, builder) ->
            likeAnyAddressLinesPredicate(joinDefendantParty(root, builder), builder, addressLine);
    }

    public static Specification<DefendantAccountEntity> likePostcode(String postcode) {
        return (root, query, builder) ->
            likePostcodePredicate(joinDefendantParty(root, builder), builder, postcode);
    }


    public static Join<DefendantAccountEntity, CourtEntity> joinEnforcingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.enforcingCourt);
    }

    public static Join<DefendantAccountEntity, CourtEntity> joinLastHearingCourt(
        Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountEntity_.parties), builder, DEFENDANT_ASSOC_TYPE);
    }

    public Specification<DefendantAccountEntity> filterByBusinessUnits(List<Short> businessUnitIds) {
        return notNullOrEmpty(businessUnitIds)
            .map(DefendantAccountSpecs::businessUnitIdsIn)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> businessUnitIdsIn(Collection<Short> businessUnitIds) {
        return (root, query, builder) -> businessUnitIdInPredicate(root, builder, businessUnitIds);
    }

    public static Predicate businessUnitIdInPredicate(From<?, DefendantAccountEntity> from, CriteriaBuilder builder,
        Collection<Short> businessUnitIds) {

        Join<DefendantAccountEntity, BusinessUnitEntity> businessUnit =
            from.join(DefendantAccountEntity_.businessUnit);
        return equalsAnyBusinessUnitIdPredicate(businessUnit, builder, businessUnitIds);
    }

    public Specification<DefendantAccountEntity> filterByActiveOnly(Boolean activeOnly) {
        return (root, query, cb) ->
            Boolean.TRUE.equals(activeOnly)
                ? cb.notEqual(root.get(DefendantAccountEntity_.accountStatus),
                DefendantAccountStatus.ACCOUNT_CONSOLIDATED)
                : cb.conjunction();
    }

    public Specification<DefendantAccountEntity> filterByAccountNumberStartsWithWithCheckLetter(
        AccountSearchDto dto) {

        return notNullObject(dto.getReferenceNumberDto())
            .map(ReferenceNumberDto::getAccountNumber)
            .flatMap(DefendantAccountSpecs::notBlankValue)
            .map(DefendantAccountSpecs::accountNumberStartsWithCheckLetter)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> accountNumberStartsWithCheckLetter(String accountNumber) {
        return (root, query, builder) -> accountNumberStartsWithCheckLetterPredicate(root, builder, accountNumber);
    }

    public static Predicate accountNumberStartsWithCheckLetterPredicate(From<?, DefendantAccountEntity> from,
        CriteriaBuilder builder, String accountNumber) {

        return likeStartsWithNormalized(
            builder,
            from.get(DefendantAccountEntity_.accountNumber),
            SpecificationUtils.stripCheckLetter(accountNumber));
    }

    public Specification<DefendantAccountEntity> filterByPcrExact(AccountSearchDto dto) {
        return notNullObject(dto.getReferenceNumberDto())
            .map(ReferenceNumberDto::getProsecutorCaseReference)
            .flatMap(DefendantAccountSpecs::notBlankValue)
            .map(DefendantAccountSpecs::prosecutorCaseReferenceEquals)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> prosecutorCaseReferenceEquals(
        String prosecutorCaseReference) {

        return (root, query, builder) -> prosecutorCaseReferenceEqualsPredicate(root, builder, prosecutorCaseReference);
    }

    public static Predicate prosecutorCaseReferenceEqualsPredicate(From<?, DefendantAccountEntity> from,
        CriteriaBuilder builder, String prosecutorCaseReference) {

        return equalNormalized(
            builder,
            from.get(DefendantAccountEntity_.prosecutorCaseReference),
            prosecutorCaseReference);
    }

    public Specification<DefendantAccountEntity> filterByDobStartsWith(AccountSearchDto dto) {
        return notNullObject(dto.getDefendant())
            .map(DefendantDto::getBirthDate)
            .map(DefendantAccountSpecs::dateOfBirthStartsWith)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> dateOfBirthStartsWith(LocalDate dateOfBirth) {
        return (root, query, builder) ->
            dateOfBirthStartsWithPredicate(joinDefendantParty(root, builder), builder, dateOfBirth);
    }

    public Specification<DefendantAccountEntity> filterByAliasesIfRequested(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Optional<DefendantDto> defendant = notNullObject(dto.getDefendant());
            if (defendant.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<DefendantAccountPartiesEntity, PartyEntity> party = joinDefendantParty(root, cb);

            return Boolean.TRUE.equals(defendant.get().getOrganisation())
                ? organisationNamePredicate(defendant.get(), party, query, cb)
                : aliasPersonNamePredicate(defendant.get(), party, query, cb);
        };
    }

    public static Predicate organisationNamePredicate(DefendantDto defendant,
        From<?, PartyEntity> party, CriteriaQuery<?> query, CriteriaBuilder builder) {

        return notBlankValue(defendant.getOrganisationName())
            .map(organisationName -> builder.or(
                partyOrganisationNamePredicate(defendant, party, builder, organisationName),
                aliasOrganisationNamePredicate(defendant, party, query, builder, organisationName)
            ))
            .orElse(builder.conjunction());
    }

    public static Predicate partyOrganisationNamePredicate(DefendantDto defendant,
        From<?, PartyEntity> party, CriteriaBuilder builder, String organisationName) {

        return builder.and(
            builder.isTrue(party.get(PartyEntity_.organisation)),
            namePredicate(
                builder,
                party.get(PartyEntity_.organisationName),
                organisationName,
                defendant.getExactMatchOrganisationName()));
    }

    public static Predicate aliasOrganisationNamePredicate(DefendantDto defendant, From<?, PartyEntity> party,
        CriteriaQuery<?> query, CriteriaBuilder builder, String organisationName) {

        if (!Boolean.TRUE.equals(defendant.getIncludeAliases())) {
            return builder.disjunction();
        }

        Root<AliasEntity> alias = query.from(AliasEntity.class);
        return builder.and(
            aliasPartyPredicate(alias, party, builder),
            builder.isTrue(party.get(PartyEntity_.organisation)),
            AliasSpecs.organisationNamePredicate(
                alias,
                builder,
                organisationName,
                defendant.getExactMatchOrganisationName()));
    }

    public static Predicate aliasPersonNamePredicate(DefendantDto defendant, From<?, PartyEntity> party,
        CriteriaQuery<?> query, CriteriaBuilder builder) {

        if (!Boolean.TRUE.equals(defendant.getIncludeAliases())) {
            return builder.conjunction();
        }

        Root<AliasEntity> alias = query.from(AliasEntity.class);
        Predicate aliasName = builder.or(optionalPredicateArray(
            notBlankValue(defendant.getSurname())
                .map(surname -> AliasSpecs.surnamePredicate(
                    alias,
                    builder,
                    surname,
                    defendant.getExactMatchSurname()
                )),
            notBlankValue(defendant.getForenames())
                .map(forenames -> AliasSpecs.forenamesPredicate(
                    alias,
                    builder,
                    forenames,
                    defendant.getExactMatchForenames()
                ))
        ));

        return aliasName.getExpressions().isEmpty()
            ? builder.conjunction()
            : builder.and(aliasPartyPredicate(alias, party, builder), aliasName);
    }

    public Specification<DefendantAccountEntity> filterByDefendantName(AccountSearchDto dto) {
        return Specification.allOf(specificationList(
            notNullObject(dto.getDefendant())
                .map(DefendantDto::getSurname)
                .flatMap(DefendantAccountSpecs::notBlankValue)
                .map(DefendantAccountSpecs::likeSurname),
            notNullObject(dto.getDefendant())
                .map(DefendantDto::getForenames)
                .flatMap(DefendantAccountSpecs::notBlankValue)
                .map(DefendantAccountSpecs::likeForename)
        ));
    }

    public Specification<DefendantAccountEntity> filterByNameIncludingAliases(AccountSearchDto dto) {
        return (root, query, cb) -> {
            query.distinct(true);
            Predicate partyPredicate = filterByDefendantName(dto).toPredicate(root, query, cb);

            return notNullObject(dto.getDefendant())
                .filter(DefendantDto::getIncludeAliases)
                .map(def -> {
                    Predicate aliasPredicate = filterByAliasesIfRequested(dto).toPredicate(root, query, cb);
                    return cb.or(partyPredicate, aliasPredicate);
                })
                .orElse(partyPredicate);
        };
    }

    public Specification<DefendantAccountEntity> filterByNiStartsWith(AccountSearchDto dto) {
        return notNullObject(dto.getDefendant())
            .map(DefendantDto::getNationalInsuranceNumber)
            .flatMap(DefendantAccountSpecs::notBlankValue)
            .map(DefendantAccountSpecs::niNumberStartsWith)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> niNumberStartsWith(String niNumber) {
        return (root, query, builder) -> niNumberStartsWithPredicate(joinDefendantParty(root, builder), builder,
            niNumber);
    }

    public Specification<DefendantAccountEntity> filterByAddress1StartsWith(AccountSearchDto dto) {
        return notNullObject(dto.getDefendant())
            .map(DefendantDto::getAddressLine1)
            .flatMap(DefendantAccountSpecs::notBlankValue)
            .map(DefendantAccountSpecs::addressLine1StartsWith)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> addressLine1StartsWith(String addressLine1) {
        return (root, query, builder) -> addressLine1StartsWithPredicate(joinDefendantParty(root, builder), builder,
            addressLine1);
    }

    public Specification<DefendantAccountEntity> filterByPostcodeStartsWith(AccountSearchDto dto) {
        return notNullObject(dto.getDefendant())
            .map(DefendantDto::getPostcode)
            .flatMap(DefendantAccountSpecs::notBlankValue)
            .map(DefendantAccountSpecs::postcodeStartsWith)
            .orElse(Specification.allOf());
    }

    public static Specification<DefendantAccountEntity> postcodeStartsWith(String postcode) {
        return (root, query, builder) ->
            postcodeStartsWithPredicate(joinDefendantParty(root, builder), builder, postcode);
    }

    @SafeVarargs
    private static Predicate[] optionalPredicateArray(Optional<Predicate>... optionalPredicates) {
        return Arrays.stream(optionalPredicates)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(Objects::nonNull)
            .toArray(Predicate[]::new);
    }

    private static Optional<Boolean> selected(Boolean candidate) {
        return Optional.ofNullable(candidate).filter(Boolean::booleanValue);
    }

    private static Optional<String> notBlankValue(String candidate) {
        return Optional.ofNullable(candidate).filter(value -> !value.isBlank());
    }
}
