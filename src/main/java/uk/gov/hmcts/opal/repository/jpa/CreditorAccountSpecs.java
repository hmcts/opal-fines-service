package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity_;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.MajorCreditorSpecs.equalsMajorCreditorIdPredicate;

public class CreditorAccountSpecs extends EntitySpecs<CreditorAccountEntity> {

    public Specification<CreditorAccountEntity> findBySearchCriteria(CreditorAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getCreditorAccountId()).map(CreditorAccountSpecs::equalsCreditorAccountId),
            numericShort(criteria.getBusinessUnitId()).map(CreditorAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(CreditorAccountSpecs::likeBusinessUnitName),
            notBlank(criteria.getAccountsNumber()).map(CreditorAccountSpecs::likeAccountsNumber),
            notBlank(criteria.getCreditorAccountType()).map(CreditorAccountSpecs::likeCreditorAccountType),
            numericLong(criteria.getMajorCreditorId()).map(CreditorAccountSpecs::equalsMajorCreditorId),
            numericLong(criteria.getMinorCreditorPartyId()).map(CreditorAccountSpecs::likeMinorCreditorPartyId),
            notBlank(criteria.getBankSortCode()).map(CreditorAccountSpecs::likeBankSortCode),
            notBlank(criteria.getBankAccountNumber()).map(CreditorAccountSpecs::likeBankAccountNumber),
            notBlank(criteria.getBankAccountName()).map(CreditorAccountSpecs::likeBankAccountName),
            notBlank(criteria.getBankAccountReference()).map(CreditorAccountSpecs::likeBankAccountReference),
            notBlank(criteria.getBankAccountType()).map(CreditorAccountSpecs::likeBankAccountType)
        ));
    }

    public static Specification<CreditorAccountEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) -> equalsCreditorAccountIdPredicate(root, builder, creditorAccountId);
    }

    public static Predicate equalsCreditorAccountIdPredicate(From<?, CreditorAccountEntity> from,
                                                             CriteriaBuilder builder, Long creditorAccountId) {
        return builder.equal(from.get(CreditorAccountEntity_.creditorAccountId), creditorAccountId);
    }


    public static Specification<CreditorAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<CreditorAccountEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<CreditorAccountEntity> likeAccountsNumber(String accountsNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.accountNumber), builder, accountsNumber);
    }

    public static Specification<CreditorAccountEntity> likeCreditorAccountType(String creditorAccountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.creditorAccountType), builder, creditorAccountType);
    }

    public static Specification<CreditorAccountEntity> equalsMajorCreditorId(Long majorCreditorId) {
        return (root, query, builder) ->
            equalsMajorCreditorIdPredicate(joinMajorCreditor(root), builder, majorCreditorId);
    }

    public static Specification<CreditorAccountEntity> likeMinorCreditorPartyId(Long minorCreditorPartyId) {
        return (root, query, builder) -> builder.equal(root.get(CreditorAccountEntity_.minorCreditorPartyId),
                                                       minorCreditorPartyId);
    }

    public static Specification<CreditorAccountEntity> likeBankSortCode(String bankSortCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankSortCode), builder, bankSortCode);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountNumber(String bankAccountNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountNumber), builder, bankAccountNumber);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountName(String bankAccountName) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountName), builder, bankAccountName);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountReference(String bankAccountReference) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountReference), builder, bankAccountReference);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountType(String bankAccountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankAccountType);
    }

    public static Join<CreditorAccountEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, CreditorAccountEntity> from) {
        return from.join(CreditorAccountEntity_.businessUnit);
    }

    public static Join<CreditorAccountEntity, MajorCreditorEntity> joinMajorCreditor(
        From<?, CreditorAccountEntity> from) {
        return from.join(CreditorAccountEntity_.majorCreditor);
    }
}
