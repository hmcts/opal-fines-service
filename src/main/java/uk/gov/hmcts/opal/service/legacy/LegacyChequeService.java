package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.service.ChequeServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyChequeService")
public class LegacyChequeService extends LegacyService implements ChequeServiceInterface {

    public LegacyChequeService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ChequeEntity getCheque(long chequeId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ChequeEntity> searchCheques(ChequeSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
