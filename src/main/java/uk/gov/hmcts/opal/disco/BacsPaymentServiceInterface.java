package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;

import java.util.List;

public interface BacsPaymentServiceInterface {

    BacsPaymentEntity getBacsPayment(long bacsPaymentId);

    List<BacsPaymentEntity> searchBacsPayments(BacsPaymentSearchDto criteria);
}
