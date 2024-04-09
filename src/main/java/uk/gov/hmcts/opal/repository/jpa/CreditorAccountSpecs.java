package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitNamePredicate;

public class CreditorAccountSpecs extends EntitySpecs<CreditorAccountEntity> {

    public Specification<CreditorAccountEntity> findBySearchCriteria(CreditorAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getCreditorAccountId()).map(CreditorAccountSpecs::equalsCreditorAccountId),
            numericShort(criteria.getBusinessUnitId()).map(CreditorAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(CreditorAccountSpecs::likeBusinessUnitName),
            notBlank(criteria.getAccountsNumber()).map(CreditorAccountSpecs::likeAccountsNumber),
            notBlank(criteria.getCreditorAccountType()).map(CreditorAccountSpecs::likeCreditorAccountType),
            notBlank(criteria.getMajorCreditorId()).map(CreditorAccountSpecs::likeMajorCreditorId),
            notBlank(criteria.getMinorCreditorPartyId()).map(CreditorAccountSpecs::likeMinorCreditorPartyId),
            notBlank(criteria.getBankSortCode()).map(CreditorAccountSpecs::likeBankSortCode),
            notBlank(criteria.getBankAccountNumber()).map(CreditorAccountSpecs::likeBankAccountNumber),
            notBlank(criteria.getBankAccountName()).map(CreditorAccountSpecs::likeBankAccountName),
            notBlank(criteria.getBankAccountReference()).map(CreditorAccountSpecs::likeBankAccountReference),
            notBlank(criteria.getBankAccountType()).map(CreditorAccountSpecs::likeBankAccountType)
        ));
    }

    public static Specification<CreditorAccountEntity> equalsCreditorAccountId(String creditorAccountId) {
        return (root, query, builder) -> builder.equal(root.get(CreditorAccountEntity_.creditorAccountId),
                                                       creditorAccountId);
    }

    public static Specification<CreditorAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            businessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<CreditorAccountEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            businessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<CreditorAccountEntity> likeAccountsNumber(String accountsNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, accountsNumber);
    }

    public static Specification<CreditorAccountEntity> likeCreditorAccountType(String creditorAccountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, creditorAccountType);
    }

    public static Specification<CreditorAccountEntity> likeMajorCreditorId(String majorCreditorId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, majorCreditorId);
    }

    public static Specification<CreditorAccountEntity> likeMinorCreditorPartyId(String minorCreditorPartyId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, minorCreditorPartyId);
    }

    public static Specification<CreditorAccountEntity> likeBankSortCode(String bankSortCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankSortCode);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountNumber(String bankAccountNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankAccountNumber);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountName(String bankAccountName) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankAccountName);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountReference(String bankAccountReference) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankAccountReference);
    }

    public static Specification<CreditorAccountEntity> likeBankAccountType(String bankAccountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorAccountEntity_.bankAccountType), builder, bankAccountType);
    }

    public static Join<CreditorAccountEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, CreditorAccountEntity> from) {
        return from.join(CreditorAccountEntity_.businessUnit);
    }
}
