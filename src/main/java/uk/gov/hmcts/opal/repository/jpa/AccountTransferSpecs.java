package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.entity.AccountTransferEntity_;

public class AccountTransferSpecs extends EntitySpecs<AccountTransferEntity> {

    public Specification<AccountTransferEntity> findBySearchCriteria(AccountTransferSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getAccountTransferId()).map(AccountTransferSpecs::equalsAccountTransferId),
            notBlank(criteria.getBusinessUnitId()).map(AccountTransferSpecs::equalsBusinessUnit),
            notBlank(criteria.getDefendantAccountId()).map(AccountTransferSpecs::equalsDefendantAccount),
            notBlank(criteria.getDocumentInstanceId()).map(AccountTransferSpecs::equalsDocumentInstanceId),
            notBlank(criteria.getReason()).map(AccountTransferSpecs::equalsReason)
        ));
    }

    public static Specification<AccountTransferEntity> equalsAccountTransferId(String accountTransferId) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.accountTransferId),
                                                       accountTransferId);
    }

    public static Specification<AccountTransferEntity> equalsBusinessUnit(String businessUnit) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.businessUnit),
                                                       businessUnit);
    }

    public static Specification<AccountTransferEntity> equalsDefendantAccount(String defendantAccount) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.defendantAccount),
                                                       defendantAccount);
    }

    public static Specification<AccountTransferEntity> equalsDocumentInstanceId(String documentInstanceId) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.documentInstanceId),
                                                       documentInstanceId);
    }

    public static Specification<AccountTransferEntity> equalsReason(String reason) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.reason),
                                                       reason);
    }

}
