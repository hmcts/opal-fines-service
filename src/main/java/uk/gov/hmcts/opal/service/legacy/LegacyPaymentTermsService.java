package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyPaymentTermsSearchResults;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.PaymentTermsServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyPaymentTermsService")
public class LegacyPaymentTermsService extends LegacyService implements PaymentTermsServiceInterface {


    public LegacyPaymentTermsService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PaymentTermsEntity getPaymentTerms(long paymentTermsId) {
        log.info("getPaymentTerms for {} from {}", paymentTermsId, legacyGateway.getUrl());
        return postToGateway("getPaymentTerms", PaymentTermsEntity.class, paymentTermsId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PaymentTermsEntity> searchPaymentTerms(PaymentTermsSearchDto criteria) {
        log.info(":searchPaymentTerms: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchPaymentTerms", LegacyPaymentTermsSearchResults.class, criteria)
            .getPaymentTermsEntities();
    }
}
