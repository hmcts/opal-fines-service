package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.entity.AccountTransferEntity_;

public class AccountTransferSpecs extends EntitySpecs<AccountTransferEntity> {

    public Specification<AccountTransferEntity> findBySearchCriteria(AccountTransferSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getAccountTransferId()).map(AccountTransferSpecs::equalsAccountTransferId)
        ));
    }

    public static Specification<AccountTransferEntity> equalsAccountTransferId(String accountTransferId) {
        return (root, query, builder) -> builder.equal(root.get(AccountTransferEntity_.accountTransferId),
                                                       accountTransferId);
    }

}
