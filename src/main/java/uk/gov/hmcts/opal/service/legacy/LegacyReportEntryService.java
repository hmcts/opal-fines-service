package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.service.ReportEntryServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyReportEntryService")
public class LegacyReportEntryService extends LegacyService implements ReportEntryServiceInterface {

    public LegacyReportEntryService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ReportEntryEntity getReportEntry(long reportEntryId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ReportEntryEntity> searchReportEntries(ReportEntrySearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
