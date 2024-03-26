package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.service.LogAuditDetailServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyLogAuditDetailService")
public class LegacyLogAuditDetailService extends LegacyService implements LogAuditDetailServiceInterface {

    public LegacyLogAuditDetailService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public LogAuditDetailEntity getLogAuditDetail(long logAuditDetailId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<LogAuditDetailEntity> searchLogAuditDetails(LogAuditDetailSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
