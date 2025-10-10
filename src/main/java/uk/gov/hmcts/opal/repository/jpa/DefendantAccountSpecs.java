package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.AliasEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.equalsCourtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartySpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsDateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeOrganisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;

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


    public static Join<DefendantAccountEntity, CourtEntity.Lite> joinEnforcingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.enforcingCourt);
    }

    public static Join<DefendantAccountEntity, CourtEntity.Lite> joinLastHearingCourt(
        Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountEntity_.parties), builder, DEFENDANT_ASSOC_TYPE);
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
                .map(SpecificationUtils::stripCheckLetter)
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
                .map(pcr -> equalNormalized(cb, root.get(DefendantAccountEntity_.prosecutorCaseReference), pcr))
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
                        party.get(PartyEntity_.birthDate),
                        cb.literal("YYYY-MM-DD"));
                    return cb.like(dobStr, dob.toString() + "%");
                })
                .orElse(cb.conjunction());
    }

    public Specification<DefendantAccountEntity> filterByAliasesIfRequested(AccountSearchDto dto) {
        return (root, query, cb) -> {
            DefendantDto def = dto.getDefendant();
            if (def == null) {
                return cb.conjunction();
            }

            // Make sure we don't get inflated counts due to joins
            query.distinct(true);

            var party = joinDefendantParty(root, cb);

            if (Boolean.TRUE.equals(def.getOrganisation())) {
                String orgName = def.getOrganisationName();
                if (orgName == null || orgName.isBlank()) {
                    return cb.conjunction();
                }

                Predicate matchOnParty = cb.and(
                    cb.isTrue(party.get(PartyEntity_.organisation)),
                    Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalNormalized(cb, party.get(PartyEntity_.organisationName), orgName)
                        : likeStartsWithNormalized(cb, party.get(PartyEntity_.organisationName), orgName)
                );

                Predicate matchOnAlias = cb.disjunction();
                if (Boolean.TRUE.equals(def.getIncludeAliases())) {
                    Root<uk.gov.hmcts.opal.entity.AliasEntity> alias =
                        query.from(uk.gov.hmcts.opal.entity.AliasEntity.class);
                    Predicate aliasJoin = cb.equal(alias.get(AliasEntity_.party)
                        .get(PartyEntity_.partyId), party.get(PartyEntity_.partyId));

                    Predicate aliasName = Boolean.TRUE.equals(def.getExactMatchOrganisationName())
                        ? equalNormalized(cb, alias.get(AliasEntity_.organisationName), orgName)
                        : likeStartsWithNormalized(cb, alias.get(AliasEntity_.organisationName), orgName);

                    // Only allow alias matches for organisation parties, and include aliasJoin
                    matchOnAlias = cb.and(aliasJoin, cb.isTrue(party.get(PartyEntity_.organisation)), aliasName);
                }

                return cb.or(matchOnParty, matchOnAlias);
            }

            // --- person-name path ---
            if (!Boolean.TRUE.equals(def.getIncludeAliases())) {
                return cb.conjunction();
            }

            Root<uk.gov.hmcts.opal.entity.AliasEntity> alias = query.from(uk.gov.hmcts.opal.entity.AliasEntity.class);
            Predicate aliasJoin = cb.equal(alias.get(AliasEntity_.party)
                .get(PartyEntity_.partyId), party.get(PartyEntity_.partyId));
            Predicate finalPredicate = cb.disjunction();

            String surname = def.getSurname();
            String forenames = def.getForenames();

            if (surname != null && !surname.isBlank()) {
                Predicate surnameMatch = Boolean.TRUE.equals(def.getExactMatchSurname())
                    ? equalNormalized(cb, alias.get(AliasEntity_.surname), surname)
                    : likeStartsWithNormalized(cb, alias.get(AliasEntity_.surname), surname);
                finalPredicate = cb.or(finalPredicate, surnameMatch);
            }

            if (forenames != null && !forenames.isBlank()) {
                Predicate forenamesMatch = Boolean.TRUE.equals(def.getExactMatchForenames())
                    ? equalNormalized(cb, alias.get(AliasEntity_.forenames), forenames)
                    : likeStartsWithNormalized(cb, alias.get(AliasEntity_.forenames), forenames);
                finalPredicate = cb.or(finalPredicate, forenamesMatch);
            }

            Predicate combined = cb.and(aliasJoin, finalPredicate);
            return finalPredicate.getExpressions().isEmpty() ? cb.conjunction() : combined;
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
