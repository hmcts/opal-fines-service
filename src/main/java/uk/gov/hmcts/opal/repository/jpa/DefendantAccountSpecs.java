package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.DateDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefendantAccountSpecs {

    public static Specification<DefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notBlank(accountSearchDto.getCourt()).map(DefendantAccountSpecs::equalsImposingCourtId),
            notBlank(accountSearchDto.getSurname()).map(DefendantAccountSpecs::likeSurname),
            notBlank(accountSearchDto.getForename()).map(DefendantAccountSpecs::likeForename),
            notBlank(accountSearchDto.getNiNumber()).map(DefendantAccountSpecs::likeNiNumber),
            notBlank(accountSearchDto.getAddressLineOne()).map(DefendantAccountSpecs::likeAddressLine1),
            notNullLocalDate(accountSearchDto.getDateOfBirth()).map(DefendantAccountSpecs::equalsDateOfBirth)
        ));
    }

    @SafeVarargs
    public static List<Specification<DefendantAccountEntity>> specificationList(
        Optional<Specification<DefendantAccountEntity>>... optionalSpecs) {
        return Arrays.stream(optionalSpecs)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public static Optional<String> notBlank(String candidate) {
        return Optional.ofNullable(candidate).filter(s -> !s.isBlank());
    }

    public static Optional<LocalDate> notNullLocalDate(DateDto candidate) {
        return Optional.ofNullable(candidate).map(DateDto::toLocalDate);
    }

    public static Specification<DefendantAccountEntity> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DefendantAccountEntity_.accountNumber), accountNo);
        };
    }

    public static Specification<DefendantAccountEntity> equalsImposingCourtId(String court) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DefendantAccountEntity_.imposingCourtId), court);
        };
    }

    public static Specification<DefendantAccountEntity> likeSurname(String surname) {
        return (root, query, builder) -> {
            return builder.like(joinParty(root).get(PartyEntity_.surname), surname);
        };
    }

    public static Specification<DefendantAccountEntity> likeForename(String forename) {
        return (root, query, builder) -> {
            return builder.like(joinParty(root).get(PartyEntity_.forenames), forename);
        };
    }

    public static Specification<DefendantAccountEntity> likeOrganisationName(String organisation) {
        return (root, query, builder) -> {
            return builder.like(joinParty(root).get(PartyEntity_.organisationName), organisation);
        };
    }

    public static Specification<DefendantAccountEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) -> {
            return builder.equal(joinParty(root).get(PartyEntity_.dateOfBirth), dob);
        };
    }

    public static Specification<DefendantAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) -> {
            return builder.equal(joinParty(root).get(PartyEntity_.niNumber), niNumber);
        };
    }

    public static Specification<DefendantAccountEntity> likeAddressLine1(String addressLine) {
        return (root, query, builder) -> {
            return builder.equal(joinParty(root).get(PartyEntity_.addressLine1), addressLine);
        };
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinParty(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.parties).join(DefendantAccountPartiesEntity_.party);
    }
}
