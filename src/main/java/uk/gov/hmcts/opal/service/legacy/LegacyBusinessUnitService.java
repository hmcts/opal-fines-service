package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyBusinessUnitService")
public class LegacyBusinessUnitService extends LegacyService implements BusinessUnitServiceInterface {

    @Autowired
    protected LegacyBusinessUnitService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public BusinessUnitEntity getBusinessUnit(long businessUnitId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
