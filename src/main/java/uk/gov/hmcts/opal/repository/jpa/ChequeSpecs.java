package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.entity.ChequeEntity_;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class ChequeSpecs extends EntitySpecs<ChequeEntity> {

    public static Specification<ChequeEntity> hasDefendantTransactionIdIn(List<Long> defendantTransactionIds) {
        return (root, query, cb) -> root.get(ChequeEntity_.defendantTransactionId).in(defendantTransactionIds);
    }

}
