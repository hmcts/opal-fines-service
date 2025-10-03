package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity_;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class BacsPaymentSpecs extends EntitySpecs<BacsPaymentEntity> {

    public static Specification<BacsPaymentEntity> hasDefendantTransactionIdIn(List<Long> defendantTransactionIds) {
        return (root, query, cb) -> root.get(BacsPaymentEntity_.defendantTransactionId).in(defendantTransactionIds);
    }

}
