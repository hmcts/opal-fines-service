package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.service.BacsPaymentServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyBacsPaymentService")
public class LegacyBacsPaymentService extends LegacyService implements BacsPaymentServiceInterface {

    public LegacyBacsPaymentService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public BacsPaymentEntity getBacsPayment(long bacsPaymentId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<BacsPaymentEntity> searchBacsPayments(BacsPaymentSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
