package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.repository.TillRepository;

@Service
@RequiredArgsConstructor
public class PreAllocatedCashTillService {

    private static final String PRE_ALLOCATED_REPORT_NAME = "Cash till report - Pre-allocated (%s)";

    private final TillRepository tillRepository;
    private final GenericReportService genericReportService;

    @Transactional
    public Long createPreAllocatedReportInstance(Long tillId, Long requestedBy, String requestedByName) {
        TillEntity till = findTill(tillId);
        CreateReportInstanceResponseReports response = genericReportService.addReportInstance(
            CreateReportInstanceRequestReports.builder()
                .reportId(CASH_TILL.getReportId())
                .businessUnitIds(List.of(getBusinessUnit(till, tillId).getBusinessUnitId()))
                .reportParameters(toReportParameters(tillId))
                .reportName(PRE_ALLOCATED_REPORT_NAME.formatted(till.getTillNumber()))
                .build(),
            requestedBy,
            requestedByName,
            false
        );
        return response.getReportInstanceId();
    }

    private TillEntity findTill(Long tillId) {
        if (tillId == null || tillId <= 0) {
            throw new IllegalArgumentException("Cash Till report till_id is required");
        }
        return tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Cash Till report till not found for till_id " + tillId));
    }

    private static BusinessUnitEntity getBusinessUnit(TillEntity till, Long tillId) {
        BusinessUnitEntity businessUnit = till.getBusinessUnit();
        if (businessUnit == null) {
            throw new EntityNotFoundException("Cash Till report business unit not found for till_id " + tillId);
        }
        return businessUnit;
    }

    private Map<String, Object> toReportParameters(Long tillId) {
        return Map.of(
            "till_id", tillId,
            "allocated_report", false
        );
    }
}
