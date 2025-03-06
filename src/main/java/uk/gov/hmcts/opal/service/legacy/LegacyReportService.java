package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.service.ReportServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyReportService")
public class LegacyReportService extends LegacyService implements ReportServiceInterface {

    public LegacyReportService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ReportEntity getReport(long reportId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ReportEntity> searchReports(ReportSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
