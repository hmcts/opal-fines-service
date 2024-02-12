package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.service.MisDebtorServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyMisDebtorService")
public class LegacyMisDebtorService extends LegacyService implements MisDebtorServiceInterface {

    @Autowired
    protected LegacyMisDebtorService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
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
