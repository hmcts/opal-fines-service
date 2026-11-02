package uk.gov.hmcts.opal.service.report;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.repository.TillRepository;

@Service
@RequiredArgsConstructor
public class TillAllocationReportService {

    private final TillRepository tillRepository;
    private final GenericReportService genericReportService;

    @Transactional
    public void createReports(Long tillId, Long requestedBy, String requestedByName) {
        TillEntity till = findTill(tillId);
        createAllocatedCashTillReport(till, requestedBy, requestedByName);
        createCashListReport(till, requestedBy, requestedByName);
    }

    private TillEntity findTill(Long tillId) {
        return tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Till not found with id: " + tillId));
    }

    private void createAllocatedCashTillReport(TillEntity till, Long requestedBy, String requestedByName) {
        CreateReportInstanceRequestReports request = reportRequest(till, ReportId.CASH_TILL,
            Map.of("till_id", till.getTillId(),
                "allocated_report", true),
            "Cash till report - Allocated (%s)".formatted(till.getTillNumber()));
        genericReportService.addReportInstance(request, requestedBy, requestedByName, false);
    }

    private void createCashListReport(TillEntity till, Long requestedBy, String requestedByName) {
        CreateReportInstanceRequestReports request = reportRequest(till, ReportId.CASH_LIST,
            Map.of("till_number", till.getTillNumber().intValue()),
            cashListTitle(till));
        genericReportService.addReportInstance(request, requestedBy, requestedByName, false);
    }

    private CreateReportInstanceRequestReports reportRequest(TillEntity till, ReportId reportId,
        Map<String, Object> parameters, String name) {

        return CreateReportInstanceRequestReports.builder()
            .reportId(reportId.getReportId())
            .businessUnitIds(List.of(till.getBusinessUnit().getBusinessUnitId()))
            .reportParameters(parameters)
            .reportName(name)
            .build();
    }

    private String cashListTitle(TillEntity till) {
        String title = "%s - %s".formatted(till.getTillNumber(), till.isAutoPayment() ? "Auto" : "Manual");
        return till.getSource() == null ? title : title + " - " + till.getSource().name();
    }
}
