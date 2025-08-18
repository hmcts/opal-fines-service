package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.AliasEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.equalsCourtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartySpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsDateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeOrganisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;

@Component
public class DefendantAccountSpecs extends EntitySpecs<DefendantAccountEntity> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

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

    public static Join<DefendantAccountEntity, CourtEntity> joinLastHearingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountEntity_.parties), builder, DEFENDANT_ASSOC_TYPE);
    }

    /* ===== normalisation helpers for AC3d/AC3e (case-insensitive, ignore spaces/hyphens/apostrophes) ===== */
    private static Expression<String> normalized(CriteriaBuilder cb, Expression<String> x) {
        Expression<String> noSpaces = cb.function("REPLACE", String.class, x, cb.literal(" "), cb.literal(""));
        Expression<String> noHyphens = cb.function("REPLACE", String.class, noSpaces, cb.literal("-"), cb.literal(""));
        Expression<String> noApos   = cb.function("REPLACE", String.class, noHyphens, cb.literal("'"), cb.literal(""));
        return cb.lower(noApos);
    }

    private static String normalizeLiteral(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase().replace(" ", "").replace("-", "").replace("'", "");
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

    public Specification<DefendantAccountEntity> filterByBusinessUnits(List<Integer> businessUnitIds) {
        return (root, query, cb) ->
            Optional.ofNullable(businessUnitIds)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    var bu = root.join(DefendantAccountEntity_.businessUnit);
                    var path = bu.get(BusinessUnitEntity_.businessUnitId);
                    var inClause = cb.in(path);
                    list.stream()
                        .filter(Objects::nonNull)
                        .map(Integer::shortValue)
                        .forEach(inClause::value);
                    return (Predicate) inClause;
                })
                .orElse(cb.conjunction());
    }



    public Specification<DefendantAccountEntity> filterByActiveOnly(Boolean activeOnly) {
        return (root, query, cb) ->
            Boolean.TRUE.equals(activeOnly)
                ? cb.notEqual(cb.upper(root.get(DefendantAccountEntity_.accountStatus)), "C")
                : cb.conjunction();
    }


    public Specification<DefendantAccountEntity> filterByAccountNumberStartsWithWithCheckLetter(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getAccountNumber)
                .filter(acc -> !acc.isBlank())
                .map(DefendantAccountSpecs::stripCheckLetter)
                .map(stripped ->
                    likeStartsWithNormalized(
                        cb,
                        root.get(DefendantAccountEntity_.accountNumber),
                        stripped
                    )
                )
                .orElse(cb.conjunction());
    }

    public Specification<DefendantAccountEntity> filterByPcrExact(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getReferenceNumberDto())
                .map(ReferenceNumberDto::getProsecutorCaseReference)
                .filter(pcr -> !pcr.isBlank())
                .map(pcr -> equalsNormalized(cb, root.get(DefendantAccountEntity_.prosecutorCaseReference), pcr))
                .orElse(cb.conjunction());
    }

    public Specification<DefendantAccountEntity> filterByDobStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getBirthDate)
                .map(dob -> {
                    var party = joinDefendantParty(root, cb);
                    Expression<String> dobStr = cb.function(
                        "to_char", String.class,
                        party.get(PartyEntity_.dateOfBirth),
                        cb.literal("YYYY-MM-DD"));
                    return cb.like(dobStr, dob.toString() + "%");
                })
                .orElse(cb.conjunction());
    }

    public Specification<DefendantAccountEntity> filterByAliasesIfRequested(AccountSearchDto dto) {
        return (root, query, cb) -> {
            DefendantDto def = dto.getDefendant();
            if (def == null || !Boolean.TRUE.equals(def.getIncludeAliases())) {
                return cb.conjunction();
            }

            var party = joinDefendantParty(root, cb);
            var alias = party.join(PartyEntity_.aliasEntities, JoinType.LEFT);

            Predicate any = cb.disjunction();

            if (Boolean.TRUE.equals(def.getOrganisation())) {
                String organisationName = def.getOrganisationName();
                if (organisationName == null || organisationName.isBlank()) {
                    return cb.conjunction();
                }
                var aliasOrganisationName = alias.get(AliasEntity_.organisationName);
                any = cb.or(any, Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                    ? equalsNormalized(cb, aliasOrganisationName, organisationName)
                    : likeStartsWithNormalized(cb, aliasOrganisationName, organisationName));
            } else {
                String surname = def.getSurname();
                String forenames = def.getForenames();

                if (surname != null && !surname.isBlank()) {
                    var aliasSurname = alias.get(AliasEntity_.surname);
                    any = cb.or(any, Boolean.TRUE.equals(def.getExactMatchSurname())
                        ? equalsNormalized(cb, aliasSurname, surname)
                        : likeStartsWithNormalized(cb, aliasSurname, surname));
                }
                if (forenames != null && !forenames.isBlank()) {
                    var aliasForenames = alias.get(AliasEntity_.forenames);
                    any = cb.or(any, Boolean.TRUE.equals(def.getExactMatchForenames())
                        ? equalsNormalized(cb, aliasForenames, forenames)
                        : likeStartsWithNormalized(cb, aliasForenames, forenames));
                }
            }

            return any.getExpressions().isEmpty() ? cb.conjunction() : any;
        };
    }

    public Specification<DefendantAccountEntity> filterByDefendantName(AccountSearchDto dto) {
        return (root, query, cb) -> {
            Optional<Predicate> surnamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getSurname)
                .filter(surname -> !surname.isBlank())
                .map(surname -> likeSurname(surname).toPredicate(root, query, cb));

            Optional<Predicate> forenamePredicate = Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getForenames)
                .filter(forenames -> !forenames.isBlank())
                .map(forenames -> likeForename(forenames).toPredicate(root, query, cb));

            return cb.and(
                surnamePredicate.orElse(cb.conjunction()),
                forenamePredicate.orElse(cb.conjunction())
            );
        };
    }

    public Specification<DefendantAccountEntity> filterByNameIncludingAliases(AccountSearchDto dto) {
        return (root, query, cb) -> {
            query.distinct(true);
            Predicate partyPredicate = filterByDefendantName(dto).toPredicate(root, query, cb);

            return Optional.ofNullable(dto.getDefendant())
                .filter(DefendantDto::getIncludeAliases)
                .map(def -> {
                    Predicate aliasPredicate = filterByAliasesIfRequested(dto).toPredicate(root, query, cb);
                    return cb.or(partyPredicate, aliasPredicate);
                })
                .orElse(partyPredicate);
        };
    }

    public Specification<DefendantAccountEntity> filterByNiStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getNationalInsuranceNumber)
                .filter(nino -> !nino.isBlank())
                .map(nino -> {
                    var party = joinDefendantParty(root, cb);
                    return likeStartsWithNormalized(cb, party.get(PartyEntity_.niNumber), nino);
                })
                .orElse(cb.conjunction());
    }


    public Specification<DefendantAccountEntity> filterByAddress1StartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getAddressLine1)
                .filter(addr -> !addr.isBlank())
                .map(addr -> {
                    var party = joinDefendantParty(root, cb);
                    return likeStartsWithNormalized(cb, party.get(PartyEntity_.addressLine1), addr);
                })
                .orElse(cb.conjunction());
    }

    public Specification<DefendantAccountEntity> filterByPostcodeStartsWith(AccountSearchDto dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto.getDefendant())
                .map(DefendantDto::getPostcode)
                .filter(postcode -> !postcode.isBlank())
                .map(postcode -> {
                    var party = joinDefendantParty(root, cb);
                    return likeStartsWithNormalized(cb, party.get(PartyEntity_.postcode), postcode);
                })
                .orElse(cb.conjunction());
    }
}
