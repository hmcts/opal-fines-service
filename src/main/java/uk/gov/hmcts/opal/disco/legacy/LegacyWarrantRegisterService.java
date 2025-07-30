package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.disco.WarrantRegisterServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyWarrantRegisterService")
public class LegacyWarrantRegisterService extends LegacyService implements WarrantRegisterServiceInterface {

    public LegacyWarrantRegisterService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public WarrantRegisterEntity getWarrantRegister(long warrantRegisterId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<WarrantRegisterEntity> searchWarrantRegisters(WarrantRegisterSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
