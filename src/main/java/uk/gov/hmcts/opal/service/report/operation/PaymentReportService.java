package uk.gov.hmcts.opal.service.report.operation;

import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_NUMBER;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_PAYMENT;
import static uk.gov.hmcts.opal.service.report.ReportType.SUMMARY;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.OperationReportSpecs;
import uk.gov.hmcts.opal.service.report.ReportCSVService;
import uk.gov.hmcts.opal.service.report.ReportId;
import uk.gov.hmcts.opal.service.report.operation.mapper.DetailedResultMapper;
import uk.gov.hmcts.opal.service.report.operation.mapper.SummaryResultMapper;

@Service
public class PaymentReportService extends AbstractOperationReportService {

    private final DefendantAccountRepository defendantAccountRepository;
    private final SummaryResultMapper summaryResultMapper;
    private final DetailedResultMapper detailedResultMapper;
    private final ObjectMapper objectMapper;
    private final PaymentReportValidator validator;

    public PaymentReportService(DefendantAccountRepository defendantAccountRepository,
        SummaryResultMapper summaryResultMapper, DetailedResultMapper detailedResultMapper, ObjectMapper objectMapper,
        PaymentReportValidator validator, ReportCSVService reportCSVService) {

        super(reportCSVService);
        this.defendantAccountRepository = defendantAccountRepository;
        this.summaryResultMapper = summaryResultMapper;
        this.detailedResultMapper = detailedResultMapper;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public ReportId getReportId() {
        return OP_PAYMENT;
    }

    @Override
    public OperationReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {
        OperationReportByPaymentFiltersDto filters = readFilters(reportInstance);
        validator.validate(filters);
        List<DefendantAccountEntity> accounts = defendantAccountRepository.findAll(
            OperationReportSpecs.accountFiltersSpec(filters),
            Sort.by(ACCOUNT_NUMBER));
        return mapReportData(filters, accounts);
    }

    @Override
    public Class<? extends OperationReportDataInterface> getStoredReportDataClass(
        ReportInstanceEntity reportInstance) {
        OperationReportByPaymentFiltersDto filters = readFilters(reportInstance);
        return filters.getReportType() == SUMMARY
            ? OperationSummaryReport.class : OperationDetailedReport.class;
    }

    private OperationReportDataInterface mapReportData(OperationReportByPaymentFiltersDto filters,
        List<DefendantAccountEntity> accounts) {

        return filters.getReportType() == SUMMARY
            ? summaryResultMapper.map(accounts) : detailedResultMapper.map(accounts);
    }

    private OperationReportByPaymentFiltersDto readFilters(ReportInstanceEntity reportInstance) {
        try {
            return objectMapper.readValue(
                reportInstance.getReportParameters(),
                OperationReportByPaymentFiltersDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse report filters", e);
        }
    }
}
