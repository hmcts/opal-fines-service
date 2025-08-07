package uk.gov.hmcts.opal.repository.jpa;

import com.networknt.schema.utils.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.equalsCourtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartySpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsDateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeInitialsPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeOrganisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;

@Slf4j
public class DefendantAccountSpecs extends EntitySpecs<DefendantAccountEntity> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

    public Specification<DefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        Optional<DefendantDto> defendant = Optional.ofNullable(accountSearchDto.getDefendant());

        //Create list of specifications based on the defendant details
        List<Specification<DefendantAccountEntity>> specificationList = new ArrayList<>(specificationList(
            defendant.map(DefendantDto::getSurname)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeSurname),

            defendant.map(DefendantDto::getForenames)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeForename),

            defendant.map(DefendantDto::getInitials)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeInitials),

            defendant.map(DefendantDto::getBirthDate)
                .map(DefendantAccountSpecs::equalsDateOfBirth),

            defendant.map(DefendantDto::getNationalInsuranceNumber)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeNiNumber),

            defendant.map(DefendantDto::getAddressLine1)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeAnyAddressLine),

            defendant.map(DefendantDto::getPostcode)
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likePostcode)));

        if (!CollectionUtils.isEmpty(accountSearchDto.getBusinessUnitIds())) {
            specificationList.add(
                equalsAnyBusinessUnitIdPredicate(accountSearchDto.getBusinessUnitIds().stream()
                    .map(Integer::shortValue)
                    .toList()
                ));
        }

        //TODO confirm how we can filter on the following criteria
        //private ReferenceNumberDto referenceNumber;

        if (accountSearchDto.getReferenceNumberDto() != null) {
            ReferenceNumberDto referenceNumberDto = accountSearchDto.getReferenceNumberDto();
            Optional.ofNullable(referenceNumberDto.getProsecutorCaseReference())
                .filter(StringUtils::isNotBlank)
                .map(DefendantAccountSpecs::likeNiNumber)
                .ifPresent(specificationList::add);
        }


        return Specification.allOf(specificationList);
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

    public static Specification<DefendantAccountEntity> likeInitials(String initials) {
        return (root, query, builder) ->
            likeInitialsPredicate(joinDefendantParty(root, builder), builder, initials);
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


    public static Specification<DefendantAccountEntity> equalsAnyBusinessUnitIdPredicate(
        Collection<Short> businessUnitIds) {
        return (root, query, builder) ->
            BusinessUnitSpecs.equalsAnyBusinessUnitIdPredicate(joinBusinessUnitEntity(root), builder, businessUnitIds);
    }


    public static Join<DefendantAccountEntity, CourtEntity> joinEnforcingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.enforcingCourt);
    }

    public static Join<DefendantAccountEntity, CourtEntity> joinLastHearingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountEntity, BusinessUnitEntity> joinBusinessUnitEntity(
        Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.businessUnit);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountEntity_.parties), builder, DEFENDANT_ASSOC_TYPE);
    }

}
