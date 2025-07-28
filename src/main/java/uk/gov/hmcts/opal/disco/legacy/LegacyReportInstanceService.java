package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.disco.ReportInstanceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyReportInstanceService")
public class LegacyReportInstanceService extends LegacyService implements ReportInstanceServiceInterface {

    public LegacyReportInstanceService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ReportInstanceEntity getReportInstance(long reportInstanceId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ReportInstanceEntity> searchReportInstances(ReportInstanceSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
