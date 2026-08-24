package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.isNullOrBlank;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.nullifyFalse;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.stripChars;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.stripCharsAndLowerOrNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_;

@Component
@Slf4j(topic = "opal.SearchDefendantAccountSpecs")
public abstract class SearchDefendantAccountSpecs<E extends SearchDefendantAccount> extends EntitySpecs<E> {

    public Specification<E> findBySearch(AccountSearchDto accSearch) {

        Optional<ReferenceNumberDto> optRefNumber = notNullObject(accSearch.getReferenceNumberDto());
        Optional<DefendantDto> optDefendant = notNullObject(accSearch.getDefendant());

        return Specification.allOf(specificationList(
            notNullObject(accSearch.getBusinessUnitIds()).map(SpecificationUtils::removeNullItems)
                .map(this::businessUnitIdIsOneOf),
            determineSearchForActiveOnly(accSearch)
                .map(this::isActive),
            optRefNumber.map(ReferenceNumberDto::getOrganisation)
                .map(this::equalsOrganisation),
            optRefNumber.map(ReferenceNumberDto::getAccountNumber).map(SpecificationUtils::stripCheckLetter)
                .map(this::likeAccountNumber),
            optRefNumber.map(ReferenceNumberDto::getProsecutorCaseReference).map(SpecificationUtils::stripCharsOrNull)
                .map(this::equalsProsecutorCaseReference),
            optDefendant.map(DefendantDto::getNationalInsuranceNumber).map(SpecificationUtils::stripCharsOrNull)
                .map(this::likeNiNumber),
            optDefendant.map(DefendantDto::getAddressLine1).map(SpecificationUtils::stripCharsOrNull)
                .map(this::likeAddressLine1),
            optDefendant.map(DefendantDto::getPostcode).map(SpecificationUtils::stripCharsOrNull)
                .map(this::likePostcode),
            optDefendant.map(DefendantDto::getBirthDate)
                .map(this::equalsDateOfBirth),
            optDefendant.map(this::matchNamesAndAliases)
        ));
    }

    public Specification<E> businessUnitIdIsOneOf(List<Short> businessUnitIds) {
        return (root, query, builder) ->
            root.get(SearchDefendantAccount_.businessUnitId).in(businessUnitIds);
    }

    public Specification<E> isActive(Boolean ignored) {
        return (root, query, builder) ->
            builder.notEqual(root.get(SearchDefendantAccount_.accountStatus),
                DefendantAccountStatus.ACCOUNT_CONSOLIDATED);
    }

    public Specification<E> equalsOrganisation(Boolean orgFlag) {
        return (root, query, cb) ->
            cb.equal(root.get(SearchDefendantAccount_.organisation), orgFlag);
    }

    public Specification<E> likeAccountNumber(String accountNo) {
        return (root, query, cb) -> likeLowerCaseBothStartsWithPredicate(
            stripChars(cb, root.get(SearchDefendantAccount_.accountNumber)), cb, accountNo);
    }

    public Specification<E> equalsProsecutorCaseReference(String pcr) {
        return (root, query, cb) -> equalsLowerCaseBothPredicate(
            stripChars(cb, root.get(SearchDefendantAccount_.prosecutorCaseReference)), cb, pcr);
    }

    public Specification<E> likeNiNumber(String niNumber) {
        return (root, query, cb) -> likeLowerCaseBothStartsWithPredicate(
            stripChars(cb, root.get(SearchDefendantAccount_.nationalInsuranceNumber)), cb, niNumber);
    }

    public Specification<E> likeAddressLine1(String address) {
        return (root, query, cb) -> likeLowerCaseBothStartsWithPredicate(
            stripChars(cb, root.get(SearchDefendantAccount_.addressLine1)), cb, address);
    }

    public Specification<E> likePostcode(String postcode) {
        return (root, query, cb) -> likeLowerCaseBothStartsWithPredicate(
            stripChars(cb, root.get(SearchDefendantAccount_.postcode)), cb, postcode);
    }

    public Specification<E> equalsDateOfBirth(LocalDate dob) {
        return (root, query, cb) -> cb.equal(root.get(SearchDefendantAccount_.birthDate), dob);
    }

    private Optional<Boolean> determineSearchForActiveOnly(AccountSearchDto accSearch) {
        return notNullObject(accSearch.getReferenceNumberDto())
            .map(refNum ->
                (isNullOrBlank(refNum.getAccountNumber()) && isNullOrBlank(refNum.getProsecutorCaseReference())))
            .or(() -> Optional.of(Boolean.TRUE))
            .map(noRefs -> nullifyFalse(
                noRefs && Optional.ofNullable(accSearch.getActiveAccountsOnly()).orElse(false)));
    }

    public Specification<E> matchNamesAndAliases(final DefendantDto defend) {
        boolean includeAliases = Boolean.TRUE.equals(defend.getIncludeAliases());
        return Boolean.TRUE.equals(defend.getOrganisation())
            ? notNullObject(defend.getOrganisationName())
            .map(SpecificationUtils::stripCharsAndLowerOrNull)
            .map(orgName -> this.matchOrgNameAndAliases(
                orgName, includeAliases, Boolean.TRUE.equals(defend.getExactMatchOrganisationName())))
            .orElse(null)
            : matchPersonNamesAndAliases(defend, includeAliases);
    }

    public Specification<E> matchOrgNameAndAliases(String orgName, boolean includeAliases, boolean exactOrgName) {
        return (root, query, cb) ->
            matchOrgNameAndAliasesPredicate(root, cb, orgName, exactOrgName, includeAliases);
    }

    private Predicate matchOrgNameAndAliasesPredicate(From<?, E> root, CriteriaBuilder cb,
        String orgName, boolean exactOrgName, boolean includeAliases) {

        List<Predicate> namePredicates = new ArrayList<>(
            aliasOrganisationPredicates(root, cb, orgName, exactOrgName, includeAliases));
        orgNamePredicate(root, cb, orgName, exactOrgName).ifPresent(namePredicates::add);

        return cb.and(
            isOrganisationPredicate(root, cb),
            cb.or(predicateArray(namePredicates)));
    }

    public Specification<E> matchPersonNamesAndAliases(DefendantDto defendant, boolean includeAliases) {

        String forenames = stripCharsAndLowerOrNull(defendant.getForenames());
        String surname = stripCharsAndLowerOrNull(defendant.getSurname());

        if (surname == null && forenames == null) {
            return null;
        }

        boolean exactForenames = Boolean.TRUE.equals(defendant.getExactMatchForenames());
        boolean exactSurname = Boolean.TRUE.equals(defendant.getExactMatchSurname());

        log.debug(":matchPersonNamesAndAliases: forenames: {}, exact: {}, surname: {}, exact: {}",
            forenames, exactForenames, surname, exactSurname);

        return (root, query, cb) -> {

            Predicate personNamesPredicate = matchPersonNamesPredicate(
                root, cb, forenames, exactForenames, surname, exactSurname);

            if (includeAliases) {
                return cb.or(personNamesPredicate,
                    personAliasesPredicate(root, cb, forenames, exactForenames, surname, exactSurname));
            } else {
                return personNamesPredicate;
            }
        };
    }

    private Predicate personAliasesPredicate(From<?, E> root, CriteriaBuilder cb,
        String forenames, boolean exactForenames, String surname, boolean exactSurname) {

        List<Predicate> aliasPredicates = new ArrayList<>(
            aliasForenamesPredicates(root, cb, forenames, exactForenames));
        aliasPredicates.addAll(aliasSurnamePredicates(root, cb, surname, exactSurname));
        Predicate[] namesAliasPredicates = predicateArray(aliasPredicates);

        return namesAliasPredicates.length == 0 ? null
            : cb.and(cb.isFalse(root.get(SearchDefendantAccount_.organisation)), cb.or(namesAliasPredicates));
    }

    private boolean hasContentToSearchOn(String candidate) {
        return candidate != null && !candidate.isBlank();
    }

    // The data return from the View with these paths are a concatenation of Alias Forename, space and Alias Surname
    private List<Path<String>> listOfAliasPaths(From<?, E> root) {
        return List.of(
            root.get(SearchDefendantAccount_.alias1),
            root.get(SearchDefendantAccount_.alias2),
            root.get(SearchDefendantAccount_.alias3),
            root.get(SearchDefendantAccount_.alias4),
            root.get(SearchDefendantAccount_.alias5));
    }

    private List<Expression<String>> strippedAliasPaths(From<?, E> root, CriteriaBuilder cb) {
        return listOfAliasPaths(root).stream()
            .map(path -> stripChars(cb, path))
            .toList();
    }

    private Optional<Predicate> orgNamePredicate(From<?, E> root, CriteriaBuilder cb,
        String orgName, boolean exactOrgName) {

        return notNullObject(orgName)
            .map(value -> useEqualsOrStartsWith(
                stripChars(cb, root.get(SearchDefendantAccount_.organisationName)),
                cb,
                value,
                exactOrgName));
    }

    private List<Predicate> aliasOrganisationPredicates(From<?, E> root, CriteriaBuilder cb,
        String orgName, boolean exactOrgName, boolean includeAliases) {

        return includeAliases
            ? aliasNamePredicates(root, cb, orgName, !exactOrgName)
            : List.of();
    }

    private Predicate isOrganisationPredicate(From<?, E> root, CriteriaBuilder cb) {
        return cb.isTrue(root.get(SearchDefendantAccount_.organisation));
    }

    private List<Predicate> aliasForenamesPredicates(From<?, E> root, CriteriaBuilder cb,
        String forenames, boolean exactForenames) {

        return hasContentToSearchOn(forenames)
            ? aliasNamePredicates(root, cb, forenames, !exactForenames)
            : List.of();
    }

    private List<Predicate> aliasSurnamePredicates(From<?, E> root, CriteriaBuilder cb,
        String surname, boolean exactSurname) {

        return hasContentToSearchOn(surname)
            ? aliasSurnameNamePredicates(root, cb, surname, exactSurname)
            : List.of();
    }

    private List<Predicate> aliasNamePredicates(
        From<?, E> root, CriteriaBuilder cb, String searchAlias, boolean useWildcard) {

        return strippedAliasPaths(root, cb).stream()
            .map(path -> useWildcardOrStartsWith(path, cb, searchAlias, useWildcard))
            .toList();
    }

    private List<Predicate> aliasSurnameNamePredicates(
        From<?, E> root, CriteriaBuilder cb, String surname, boolean exactSurname) {

        return listOfAliasPaths(root).stream()
            .map(path -> aliasSurnamePath(path, cb))
            .map(path -> useEqualsOrStartsWith(path, cb, surname, exactSurname))
            .toList();
    }

    private Expression<String> aliasSurnamePath(Path<String> aliasPath, CriteriaBuilder cb) {
        return stripChars(cb, cb.function(
            "regexp_replace", String.class, aliasPath, cb.literal("^.*\\s+"), cb.literal("")));
    }

    public Predicate matchPersonNamesPredicate(From<?, E> from, CriteriaBuilder cb,
        String forenames, boolean exactForenames, String surname, boolean exactSurname) {

        return cb.and(predicateArray(
            likePersonForenamesPredicate(from, cb, forenames, exactForenames),
            likePersonSurnamePredicate(from, cb, surname, exactSurname)
        ));
    }

    public Optional<Predicate> likePersonSurnamePredicate(
        From<?, E> from, CriteriaBuilder cb, String surname, boolean exactSurname) {
        return notNullObject(surname)
            .map(sn -> useEqualsOrStartsWith(
                stripChars(cb, from.get(SearchDefendantAccount_.surname)), cb, sn, exactSurname));
    }

    public Optional<Predicate> likePersonForenamesPredicate(
        From<?, E> from, CriteriaBuilder cb, String forenames, boolean exactForenames) {
        return notNullObject(forenames)
            .map(fn -> useEqualsOrStartsWith(
                stripChars(cb, from.get(SearchDefendantAccount_.forenames)), cb, fn, exactForenames));
    }

    private Predicate useEqualsOrStartsWith(Expression<String> dbPath, CriteriaBuilder cb,
        String comparisonText, boolean useEquals) {

        return useEquals ? equalsLowerCasePredicate(dbPath, cb, comparisonText)
            : likeLowerCaseStartsWithPredicate(dbPath, cb, comparisonText);
    }

    private Predicate useWildcardOrStartsWith(Expression<String> dbPath, CriteriaBuilder cb,
        String comparisonText, boolean useWildcard) {

        return useWildcard
            ? likeLowerCaseWildcardPredicate(dbPath, cb, comparisonText)
            : likeLowerCaseStartsWithPredicate(dbPath, cb, comparisonText);
    }

}
