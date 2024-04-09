package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.service.CreditorAccountServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyCreditorAccountService")
public class LegacyCreditorAccountService extends LegacyService implements CreditorAccountServiceInterface {

    public LegacyCreditorAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CreditorAccountEntity getCreditorAccount(long creditorAccountId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<CreditorAccountEntity> searchCreditorAccounts(CreditorAccountSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
