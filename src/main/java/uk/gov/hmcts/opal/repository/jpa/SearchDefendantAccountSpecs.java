package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity_;

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

    public static Specification<SearchDefendantAccountEntity> likeOrganisationName(String organisationName) {
        return (root, query, cb) ->
            likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), organisationName);
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

    /* -------------------- Filters that remain meaningful on the flat view -------------------- */

    public Specification<SearchDefendantAccountEntity> filterByBusinessUnits(List<Integer> businessUnitIds) {
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

    public Specification<SearchDefendantAccountEntity> filterByActiveOnly(Boolean activeOnly) {
        return (root, query, cb) ->
            Boolean.TRUE.equals(activeOnly)
                ? cb.notEqual(cb.upper(root.get(SearchDefendantAccountEntity_.accountStatus)), "C")
                : cb.conjunction();
    }

    public Specification<SearchDefendantAccountEntity> filterByAccountNumberStartsWithWithCheckLetter(
        AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getAccountNumber)
                .filter(acc -> !acc.isBlank())
                .map(SearchDefendantAccountSpecs::stripCheckLetter)
                .map(stripped -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.accountNumber),
                                                          stripped))
                .orElse(cb.conjunction());
    }

    public Specification<SearchDefendantAccountEntity> filterByPcrExact(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getProsecutorCaseReference)
                .filter(pcr -> !pcr.isBlank())
                .map(pcr -> equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.prosecutorCaseReference), pcr))
                .orElse(cb.conjunction());
    }

    public Specification<SearchDefendantAccountEntity> filterByDobStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getBirthDate)
                .map(dob -> {
                    Expression<String> dobStr = cb.function(
                        "to_char", String.class,
                        root.get(SearchDefendantAccountEntity_.birthDate),
                        cb.literal("YYYY-MM-DD"));
                    return cb.like(dobStr, dob.toString() + "%");
                })
                .orElse(cb.conjunction());
    }

    /**
     * Alias matching without joining alias table: uses alias_* columns exposed by the view.
     */
    public Specification<SearchDefendantAccountEntity> filterByAliasesIfRequested(AccountSearchDto dto) {
        return (root, query, cb) -> {
            DefendantDto def = dto.getDefendant();
            if (def == null) {
                return cb.conjunction();
            }

            // Handy handles to the 5 alias columns on the view
            Expression<String> a1 = root.get("alias1");
            Expression<String> a2 = root.get("alias2");
            Expression<String> a3 = root.get("alias3");
            Expression<String> a4 = root.get("alias4");
            Expression<String> a5 = root.get("alias5");

            /* ---------------- Organisation path ---------------- */
            if (Boolean.TRUE.equals(def.getOrganisation())) {
                String orgName = def.getOrganisationName();
                if (orgName == null || orgName.isBlank()) {
                    return cb.conjunction();
                }

                Predicate isOrg = cb.isTrue(root.get(SearchDefendantAccountEntity_.organisation));

                Predicate onParty = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                    ? equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName)
                    : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName);

                Predicate onAlias = cb.disjunction();
                if (Boolean.TRUE.equals(def.getIncludeAliases())) {
                    Predicate a1p = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, a1, orgName) : likeStartsWithNormalized(cb, a1, orgName);
                    Predicate a2p = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, a2, orgName) : likeStartsWithNormalized(cb, a2, orgName);
                    Predicate a3p = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, a3, orgName) : likeStartsWithNormalized(cb, a3, orgName);
                    Predicate a4p = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, a4, orgName) : likeStartsWithNormalized(cb, a4, orgName);
                    Predicate a5p = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, a5, orgName) : likeStartsWithNormalized(cb, a5, orgName);
                    onAlias = cb.or(a1p, a2p, a3p, a4p, a5p);
                }

                return cb.and(isOrg, cb.or(onParty, onAlias));
            }

            /* ---------------- Person path ---------------- */
            if (!Boolean.TRUE.equals(def.getIncludeAliases())) {
                return cb.conjunction();
            }

            Predicate isPerson = cb.isFalse(root.get(SearchDefendantAccountEntity_.organisation));
            Predicate combined = cb.disjunction();

            // Forenames: aliases are "forenames surname" → starts-with covers both exact & partial
            String forenames = def.getForenames();
            if (forenames != null && !forenames.isBlank()) {
                Predicate f1 = likeStartsWithNormalized(cb, a1, forenames);
                Predicate f2 = likeStartsWithNormalized(cb, a2, forenames);
                Predicate f3 = likeStartsWithNormalized(cb, a3, forenames);
                Predicate f4 = likeStartsWithNormalized(cb, a4, forenames);
                Predicate f5 = likeStartsWithNormalized(cb, a5, forenames);
                combined = cb.or(combined, f1, f2, f3, f4, f5);
            }

            // Surname: aliases are "forenames surname" → check end/contain using normalized() (no spaces)
            String surname = def.getSurname();
            if (surname != null && !surname.isBlank()) {
                String s = normalizeLiteral(surname);

                // exact = ends-with surname (or alias equals surname when no forenames stored)
                Predicate s1;
                Predicate s2;
                Predicate s3;
                Predicate s4;
                Predicate s5;

                if (Boolean.TRUE.equals(def.getExactMatchSurname())) {
                    s1 = cb.or(equalsNormalized(cb, a1, surname), cb.like(normalized(cb, a1), "%" + s));
                    s2 = cb.or(equalsNormalized(cb, a2, surname), cb.like(normalized(cb, a2), "%" + s));
                    s3 = cb.or(equalsNormalized(cb, a3, surname), cb.like(normalized(cb, a3), "%" + s));
                    s4 = cb.or(equalsNormalized(cb, a4, surname), cb.like(normalized(cb, a4), "%" + s));
                    s5 = cb.or(equalsNormalized(cb, a5, surname), cb.like(normalized(cb, a5), "%" + s));
                } else {
                    // partial = contains surname letters anywhere
                    s1 = cb.like(normalized(cb, a1), "%" + s + "%");
                    s2 = cb.like(normalized(cb, a2), "%" + s + "%");
                    s3 = cb.like(normalized(cb, a3), "%" + s + "%");
                    s4 = cb.like(normalized(cb, a4), "%" + s + "%");
                    s5 = cb.like(normalized(cb, a5), "%" + s + "%");
                }

                combined = cb.or(combined, s1, s2, s3, s4, s5);
            }

            return combined.getExpressions().isEmpty() ? cb.conjunction() : cb.and(isPerson, combined);
        };
    }

    public Specification<SearchDefendantAccountEntity> filterByDefendantName(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Optional<Predicate> surnamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getSurname)
                .filter(surname -> !surname.isBlank())
                .map(surname -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.surname), surname));

            Optional<Predicate> forenamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getForenames)
                .filter(forenames -> !forenames.isBlank())
                .map(forenames -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.forenames),
                                                           forenames));

            return cb.and(
                surnamePredicate.orElse(cb.conjunction()),
                forenamePredicate.orElse(cb.conjunction())
            );
        };
    }

    public Specification<SearchDefendantAccountEntity> filterByNameIncludingAliases(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Predicate partyPredicate = filterByDefendantName(dto).toPredicate(root, query, cb);

            return Optional.ofNullable(dto.getDefendant())
                .filter(DefendantDto::getIncludeAliases)
                .map(def -> cb.or(partyPredicate, filterByAliasesIfRequested(dto).toPredicate(root, query, cb)))
                .orElse(partyPredicate);
        };
    }

    public Specification<SearchDefendantAccountEntity> filterByNiStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getNationalInsuranceNumber)
                .filter(nino -> !nino.isBlank())
                .map(nino -> likeStartsWithNormalized(cb, root.get(
                    SearchDefendantAccountEntity_.nationalInsuranceNumber), nino))
                .orElse(cb.conjunction());
    }

    public Specification<SearchDefendantAccountEntity> filterByAddress1StartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getAddressLine1)
                .filter(addr -> !addr.isBlank())
                .map(addr -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.addressLine1), addr))
                .orElse(cb.conjunction());
    }

    public Specification<SearchDefendantAccountEntity> filterByPostcodeStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getPostcode)
                .filter(postcode -> !postcode.isBlank())
                .map(postcode -> likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.postcode),
                                                          postcode))
                .orElse(cb.conjunction());
    }

    /* ===== normalisation helpers for AC3d/AC3e (case-insensitive, ignore spaces/hyphens/apostrophes) ===== */
    private static Expression<String> normalized(CriteriaBuilder cb, Expression<String> x) {
        Expression<String> noSpaces = cb.function("REPLACE", String.class, x, cb.literal(" "), cb.literal(""));
        Expression<String> noHyphens = cb.function("REPLACE", String.class, noSpaces, cb.literal("-"),
                                                   cb.literal(""));
        Expression<String> noApos   = cb.function("REPLACE", String.class, noHyphens, cb.literal("'"),
                                                  cb.literal(""));
        return cb.lower(noApos);
    }

    private static String normalizeLiteral(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase().replace(" ", "").replace("-", "").replace("'",
                                                                         "");
    }

    private static Predicate likeStartsWithNormalized(CriteriaBuilder cb, Expression<String> field, String value) {
        return cb.like(normalized(cb, field), normalizeLiteral(value) + "%");
    }

    private static Predicate equalsNormalized(CriteriaBuilder cb, Expression<String> field, String value) {
        return cb.equal(normalized(cb, field), normalizeLiteral(value));
    }

    private static String stripCheckLetter(String acc) {
        if (acc == null) {
            return null;
        }
        return (acc.length() == 9 && Character.isLetter(acc.charAt(8))) ? acc.substring(0, 8) : acc;
    }
}
