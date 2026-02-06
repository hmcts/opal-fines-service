package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_;

import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.isNullOrBlank;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.nullifyFalse;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.stripChars;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.stripCharsAndLowerOrNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
            builder.notEqual(builder.upper(root.get(SearchDefendantAccount_.accountStatus)), "C");
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

        Expression<String> stripedDbOrg = stripChars(cb, root.get(SearchDefendantAccount_.organisationName));
        Predicate orgNamePredicate = useEqualsOrStartsWith(stripedDbOrg, cb, orgName, exactOrgName);

        List<Predicate> predicatesToOr = new ArrayList<>(Collections.singletonList(orgNamePredicate));;

        if (includeAliases) {
            predicatesToOr.addAll(
                applyPredicateFunctionToEachPath(
                    aliasPath -> useEqualsOrStartsWith(aliasPath, cb, orgName, exactOrgName),
                listOfStipedAliasPaths(root, cb)));
        }

        Predicate isOrgPredicate = cb.isTrue(root.get(SearchDefendantAccount_.organisation));
        return cb.and(isOrgPredicate, cb.or(predicatesToOr.toArray(new Predicate[]{})));
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

        List<Predicate> predicatesToOr = new ArrayList<>();

        if (hasContentToSearchOn(forenames)) {
            predicatesToOr.addAll(listOfAliasEqualsOrStartsWithPredicates(root, cb, forenames, !exactForenames, false));
        }

        if (hasContentToSearchOn(surname)) {
            predicatesToOr.addAll(listOfAliasEqualsOrStartsWithPredicates(root, cb, surname, !exactSurname, true));
        }

        Predicate[] namesAliasPredicates = predicateArray(predicatesToOr);

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
            root.get(SearchDefendantAccount_.alias5)
        );
    }

    private List<Expression<String>> listOfStipedAliasPaths(From<?, E> root, CriteriaBuilder cb) {
        return listOfAliasPaths(root).stream()
            .map(path -> stripChars(cb, path))
            .toList();
    }

    private List<Predicate> listOfAliasEqualsOrStartsWithPredicates(
        From<?, E> root, CriteriaBuilder cb, String searchAlias, boolean useWildcard, boolean useEndsWith) {

        // Match the Forename to the start, and the Surname to the end, of the concatenated data.
        return listOfStipedAliasPaths(root, cb).stream()
            .map(path -> useEndsWithOrStartsWith(path, cb, searchAlias, useWildcard, useEndsWith))
            .toList();
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

    private List<Predicate> applyPredicateFunctionToEachPath(Function<Expression<String>, Predicate> fn,
        Collection<? extends Expression<String>> aliasPaths) {
        return aliasPaths.stream().map(fn).toList();
    }

    private Predicate useEqualsOrStartsWith(Expression<String> dbPath, CriteriaBuilder cb,
        String comparisonText, boolean useEquals) {

        return useEquals ? equalsLowerCasePredicate(dbPath, cb, comparisonText)
            : likeLowerCaseStartsWithPredicate(dbPath, cb, comparisonText);
    }

    private Predicate useEndsWithOrStartsWith(Expression<String> dbPath, CriteriaBuilder cb,
        String comparisonText, boolean useWildcard, boolean useEndsWith) {

        return useWildcard
            ? likeLowerCaseWildcardPredicate(dbPath, cb, comparisonText)
            : useEndsWith
                ? likeLowerCaseEndsWithPredicate(dbPath, cb, comparisonText)
                : likeLowerCaseStartsWithPredicate(dbPath, cb, comparisonText);
    }

}
