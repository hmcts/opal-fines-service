package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.service.PaymentInServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyPaymentInService")
public class LegacyPaymentInService extends LegacyService implements PaymentInServiceInterface {

    public LegacyPaymentInService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PaymentInEntity getPaymentIn(long paymentInId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<PaymentInEntity> searchPaymentIns(PaymentInSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
