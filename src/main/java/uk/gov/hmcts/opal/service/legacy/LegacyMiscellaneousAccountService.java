package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyMiscellaneousAccountService")
public class LegacyMiscellaneousAccountService extends LegacyService implements MiscellaneousAccountServiceInterface {

    public LegacyMiscellaneousAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public MiscellaneousAccountEntity getMiscellaneousAccount(long miscellaneousAccountId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<MiscellaneousAccountEntity> searchMiscellaneousAccounts(MiscellaneousAccountSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
