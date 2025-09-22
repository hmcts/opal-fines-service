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

            // Organisation search path
            if (Boolean.TRUE.equals(def.getOrganisation())) {
                String orgName = def.getOrganisationName();
                if (orgName == null || orgName.isBlank()) {
                    return cb.conjunction();
                }

                Predicate onParty = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                    ? equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName)
                    : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.organisationName), orgName);

                Predicate onAlias = cb.disjunction();
                if (Boolean.TRUE.equals(def.getIncludeAliases())) {
                    onAlias = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasOrganisationName), orgName)
                        : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasOrganisationName),
                                                   orgName);
                }
                return cb.or(onParty, onAlias);
            }

            // Person-name path
            if (!Boolean.TRUE.equals(def.getIncludeAliases())) {
                return cb.conjunction();
            }

            Predicate p = cb.disjunction();
            String surname = def.getSurname();
            String forenames = def.getForenames();

            if (surname != null && !surname.isBlank()) {
                Predicate surnameMatch = Boolean.TRUE.equals(def.getExactMatchSurname())
                    ? equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasSurname), surname)
                    : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasSurname), surname);
                p = cb.or(p, surnameMatch);
            }

            if (forenames != null && !forenames.isBlank()) {
                Predicate forenamesMatch = Boolean.TRUE.equals(def.getExactMatchForenames())
                    ? equalsNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasForenames), forenames)
                    : likeStartsWithNormalized(cb, root.get(SearchDefendantAccountEntity_.aliasForenames), forenames);
                p = cb.or(p, forenamesMatch);
            }

            return p.getExpressions().isEmpty() ? cb.conjunction() : p;
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
