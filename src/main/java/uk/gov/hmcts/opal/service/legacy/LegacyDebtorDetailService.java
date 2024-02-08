package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.DebtorDetailServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDebtorDetailService")
public class LegacyDebtorDetailService extends LegacyService implements DebtorDetailServiceInterface {

    @Autowired
    protected LegacyDebtorDetailService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DebtorDetailEntity getDebtorDetail(long debtorDetailId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
