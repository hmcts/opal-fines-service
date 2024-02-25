package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.service.MisDebtorServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyMisDebtorService")
public class LegacyMisDebtorService extends LegacyService implements MisDebtorServiceInterface {


    public LegacyMisDebtorService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public MisDebtorEntity getMisDebtor(long misDebtorId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<MisDebtorEntity> searchMisDebtors(MisDebtorSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
