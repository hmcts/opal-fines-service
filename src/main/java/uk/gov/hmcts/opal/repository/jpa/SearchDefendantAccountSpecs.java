package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity_;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.normalize;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.normalizeExpr;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class SearchDefendantAccountSpecs extends EntitySpecs<SearchDefendantAccountEntity> {

    public Specification<SearchDefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getSurname)
                .map(SearchDefendantAccountSpecs::likeSurname),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getForenames)
                .map(SearchDefendantAccountSpecs::likeForename),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getBirthDate)
                .map(SearchDefendantAccountSpecs::equalsDateOfBirth),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getNationalInsuranceNumber)
                .map(SearchDefendantAccountSpecs::likeNiNumber),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getAddressLine1)
                .map(SearchDefendantAccountSpecs::likeAnyAddressLine),
            notNullObject(accountSearchDto.getDefendant()).map(DefendantDto::getPostcode)
                .map(SearchDefendantAccountSpecs::likePostcode)
        ));
    }

    public static Predicate equalsDefendantAccountIdPredicate(
        From<?, SearchDefendantAccountEntity> from, CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(SearchDefendantAccountEntity_.defendantAccountId), defendantAccountId);
    }

    public static Specification<SearchDefendantAccountEntity> equalsAccountNumber(String accountNo) {
        return (root, query, cb) -> cb.equal(root.get(SearchDefendantAccountEntity_.accountNumber), accountNo);
    }

    /* -------------------- Flat-field predicates (no joins) -------------------- */

    public static Specification<SearchDefendantAccountEntity> likeSurname(String surname) {
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.surname), surname);
    }

    public static Specification<SearchDefendantAccountEntity> likeForename(String forename) {
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.forenames), forename);
    }

    public static Specification<SearchDefendantAccountEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, cb) -> cb.equal(root.get(SearchDefendantAccountEntity_.birthDate), dob);
    }

    public static Specification<SearchDefendantAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.nationalInsuranceNumber), niNumber);
    }

    public static Specification<SearchDefendantAccountEntity> likeAnyAddressLine(String addressLine) {
        // Flat view gives address_line_1; apply to that column
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.addressLine1), addressLine);
    }

    public static Specification<SearchDefendantAccountEntity> likePostcode(String postcode) {
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.postcode), postcode);
    }

    public static Specification<SearchDefendantAccountEntity> findBySearch(AccountSearchDto accountSearchDto) {

        boolean hasRef =
            Optional.ofNullable(accountSearchDto.getReferenceNumberDto())
                .map(refNo ->
                    isNotBlank(refNo.getAccountNumber()) || isNotBlank(refNo.getProsecutorCaseReference()))
                .orElse(false);

        boolean applyActiveOnly =
            Boolean.TRUE.equals(accountSearchDto.getActiveAccountsOnly()) && !hasRef;

        return filterByBusinessUnits(accountSearchDto.getBusinessUnitIds())
            .and(filterByActiveOnly(applyActiveOnly))
            .and(filterByAccountNumberStartsWithWithCheckLetter(accountSearchDto))
            .and(filterByPcrExact(accountSearchDto))
            .and(filterByReferenceOrganisationFlag(accountSearchDto))
            .and(
                accountSearchDto.getDefendant() != null
                    && Boolean.TRUE.equals(accountSearchDto.getDefendant().getOrganisation())
                    ? filterByAliasesIfRequested(accountSearchDto)
                    : filterByNameIncludingAliases(accountSearchDto)
            )
            .and(filterByDobStartsWith(accountSearchDto))
            .and(filterByNiStartsWith(accountSearchDto))
            .and(filterByAddress1StartsWith(accountSearchDto))
            .and(filterByPostcodeStartsWith(accountSearchDto));
    }

    /* -------------------- Filters that remain meaningful on the flat view -------------------- */

    public static Specification<SearchDefendantAccountEntity> filterByBusinessUnits(List<Integer> businessUnitIds) {
        return (root, query, cb) ->
            Optional.ofNullable(businessUnitIds)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    var path = root.get(SearchDefendantAccountEntity_.businessUnitId);
                    var inClause = cb.in(path);
                    list.stream()
                        .filter(Objects::nonNull)
                        .map(Integer::longValue)   // compare against Long
                        .forEach(inClause::value);
                    return (Predicate) inClause;
                })
                .orElse(cb.conjunction());
    }

    public static Specification<SearchDefendantAccountEntity> filterByActiveOnly(Boolean activeOnly) {
        return (root, query, cb) ->
            Boolean.TRUE.equals(activeOnly)
                ? cb.notEqual(cb.upper(root.get(SearchDefendantAccountEntity_.accountStatus)), "C")
                : cb.conjunction();
    }

    public static Specification<SearchDefendantAccountEntity> filterByAccountNumberStartsWithWithCheckLetter(
        AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getAccountNumber)
                .filter(acc -> !acc.isBlank())
                .map(SpecificationUtils::stripCheckLetter)
                .map(stripped -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.accountNumber),
                                                          stripped
                ))
                .orElse(cb.conjunction());
    }

    public static Specification<SearchDefendantAccountEntity> filterByPcrExact(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getProsecutorCaseReference)
                .filter(pcr -> !pcr.isBlank())
                .map(pcr -> equalNormalized(cb, root.get(SearchDefendantAccountEntity_.prosecutorCaseReference), pcr))
                .orElse(cb.conjunction());
    }

    public static Specification<SearchDefendantAccountEntity> filterByDobStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getBirthDate)
                .map(dob -> {
                    Expression<String> dobStr = cb.function(
                        "to_char", String.class,
                        root.get(SearchDefendantAccountEntity_.birthDate),
                        cb.literal("YYYY-MM-DD")
                    );
                    return cb.like(dobStr, dob.toString() + "%");
                })
                .orElse(cb.conjunction());
    }

    /**
     * Alias matching without joining alias table: uses alias_* columns exposed by the view.
     */
    public static Specification<SearchDefendantAccountEntity> filterByAliasesIfRequested(AccountSearchDto dto) {
        return (root, query, cb) -> {
            DefendantDto def = dto.getDefendant();
            if (def == null) {
                return cb.conjunction();
            }

            return Boolean.TRUE.equals(def.getOrganisation())
                ? orgAliases(def, root, cb)
                : personAliases(def, root, cb);
        };
    }

    private static Predicate orgAliases(DefendantDto def,
                                 From<?, SearchDefendantAccountEntity> root,
                                 CriteriaBuilder cb) {
        String orgName = def.getOrganisationName();
        if (orgName == null || orgName.isBlank()) {
            return cb.conjunction();
        }

        Predicate isOrg = cb.isTrue(root.get(SearchDefendantAccountEntity_.organisation));

        Predicate onParty = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
            ? equalNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName)
            : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName);

        Predicate onAlias = cb.disjunction();
        if (Boolean.TRUE.equals(def.getIncludeAliases())) {
            boolean exact = Boolean.TRUE.equals(def.getExactMatchOrganisationName());
            onAlias = orAcrossAliases(
                cb,
                cb.disjunction(),
                alias -> exact ? equalNormalized(cb, alias, orgName)
                    : likeStartsWithNormalized(cb, alias, orgName),
                aliases(root)
            );
        }

        return cb.and(isOrg, cb.or(onParty, onAlias));
    }

    private static List<Expression<String>> aliases(From<?, SearchDefendantAccountEntity> root) {
        return List.of(
            root.get("alias1"),
            root.get("alias2"),
            root.get("alias3"),
            root.get("alias4"),
            root.get("alias5")
        );
    }

    private static Predicate personAliases(DefendantDto def,
                                    From<?, SearchDefendantAccountEntity> root,
                                    CriteriaBuilder cb) {
        if (!Boolean.TRUE.equals(def.getIncludeAliases())) {
            return cb.conjunction();
        }

        Predicate isPerson = cb.isFalse(root.get(SearchDefendantAccountEntity_.organisation));
        var aliasExprs = aliases(root);

        Predicate combined = cb.disjunction();

        String forenames = def.getForenames();
        if (forenames != null && !forenames.isBlank()) {
            for (Expression<String> a : aliasExprs) {
                combined = cb.or(combined, likeStartsWithNormalized(cb, a, forenames));
            }
        }

        String surname = def.getSurname();
        if (surname != null && !surname.isBlank()) {
            boolean exact = Boolean.TRUE.equals(def.getExactMatchSurname());
            combined = orAcrossAliases(
                cb, combined,
                alias -> surnamePredicate(cb, alias, surname, exact),
                aliasExprs
            );
        }

        return combined.getExpressions().isEmpty() ? cb.conjunction() : cb.and(isPerson, combined);
    }



    public static Specification<SearchDefendantAccountEntity> filterByDefendantName(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Optional<Predicate> surnamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getSurname)
                .filter(surname -> !surname.isBlank())
                .map(surname -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.surname), surname));

            Optional<Predicate> forenamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getForenames)
                .filter(forenames -> !forenames.isBlank())
                .map(forenames -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.forenames),
                                                           forenames
                ));

            return cb.and(
                surnamePredicate.orElse(cb.conjunction()),
                forenamePredicate.orElse(cb.conjunction())
            );
        };
    }

    public static Specification<SearchDefendantAccountEntity> filterByNameIncludingAliases(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Predicate partyPredicate = filterByDefendantName(dto).toPredicate(root, query, cb);

            return Optional.ofNullable(dto.getDefendant())
                .filter(DefendantDto::getIncludeAliases)
                .map(def -> cb.or(partyPredicate, filterByAliasesIfRequested(dto).toPredicate(root, query, cb)))
                .orElse(partyPredicate);
        };
    }

    public static Specification<SearchDefendantAccountEntity> filterByNiStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getNationalInsuranceNumber)
                .filter(nino -> !nino.isBlank())
                .map(nino -> likeStartsWithNormalized(cb, root.get(
                    SearchDefendantAccountEntity_.nationalInsuranceNumber), nino))
                .orElse(cb.conjunction());
    }

    public static Specification<SearchDefendantAccountEntity> filterByAddress1StartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getAddressLine1)
                .filter(addr -> !addr.isBlank())
                .map(addr -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.addressLine1), addr))
                .orElse(cb.conjunction());
    }

    public static Specification<SearchDefendantAccountEntity> filterByPostcodeStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getPostcode)
                .filter(postcode -> !postcode.isBlank())
                .map(postcode -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.postcode),
                                                          postcode
                ))
                .orElse(cb.conjunction());
    }

    private static Predicate orAcrossAliases(CriteriaBuilder cb,
                                      Predicate seed,
                                      Function<Expression<String>, Predicate> fn,
                                      Iterable<Expression<String>> aliases) {
        Predicate acc = seed;
        for (Expression<String> a : aliases) {
            acc = cb.or(acc, fn.apply(a));
        }
        return acc;
    }

    private static Predicate surnamePredicate(CriteriaBuilder cb, Expression<String> aliasExpr,
                                       String surname, boolean exact) {
        String s = normalize(surname);
        return exact
            ? cb.or(equalNormalized(cb, aliasExpr, surname),
                    cb.like(normalizeExpr(cb, aliasExpr), "%" + s))
            : cb.like(normalizeExpr(cb, aliasExpr), "%" + s + "%");
    }
    /**
     * Filters results by reference_number.organisation flag when provided.
     * PO-2298 – ensures that reference searches honour organisation = true/false.
     */

    public static Specification<SearchDefendantAccountEntity> filterByReferenceOrganisationFlag(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getOrganisation)
                .map(orgFlag -> cb.equal(root.get(SearchDefendantAccountEntity_.organisation), orgFlag))
                .orElse(cb.conjunction());
    }
}
