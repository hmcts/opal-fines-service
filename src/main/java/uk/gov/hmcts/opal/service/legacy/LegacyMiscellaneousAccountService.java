package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMiscellaneousAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyMiscellaneousAccountService")
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
        log.debug("getMiscellaneousAccount for {} from {}", miscellaneousAccountId, legacyGateway.getUrl());
        return postToGateway("getMiscellaneousAccount", MiscellaneousAccountEntity.class, miscellaneousAccountId);
    }

    @Override
    public List<MiscellaneousAccountEntity> searchMiscellaneousAccounts(MiscellaneousAccountSearchDto criteria) {
        log.debug(":searchMiscellaneousAccounts: criteria: {} via gateway {}", criteria.toJson(),
                 legacyGateway.getUrl());
        return postToGateway("searchMiscellaneousAccounts", LegacyMiscellaneousAccountSearchResults.class,
                             criteria)
            .getMiscellaneousAccountEntities();
    }

}
