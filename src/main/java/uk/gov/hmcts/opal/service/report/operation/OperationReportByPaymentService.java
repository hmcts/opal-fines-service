package uk.gov.hmcts.opal.service.report.operation;

import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_NUMBER;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_PAYMENT;
import static uk.gov.hmcts.opal.service.report.ReportType.SUMMARY;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.OperationReportSpecs;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportId;
import uk.gov.hmcts.opal.service.report.ReportInterface;
import uk.gov.hmcts.opal.service.report.operation.mapper.CommonResultMapper;
import uk.gov.hmcts.opal.service.report.operation.mapper.DetailedResultMapper;
import uk.gov.hmcts.opal.service.report.operation.mapper.SummaryResultMapper;

@Service
@RequiredArgsConstructor
public class OperationReportByPaymentService implements ReportInterface<ReportDataInterface> {

    private final DefendantAccountRepository defendantAccountRepository;
    private final SummaryResultMapper summaryResultMapper;
    private final DetailedResultMapper detailedResultMapper;
    private final ObjectMapper objectMapper;
    private final OperationReportByPaymentValidator validator;

    @Override
    public ReportId getReportId() {
        return OP_PAYMENT;
    }

    @Override
    public ReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {
        OperationReportByPaymentFiltersDto filters = readFilters(reportInstance);
        validator.validate(filters);
        CommonResultMapper resultMapper =
            filters.getReportType() == SUMMARY
                ? summaryResultMapper
                : detailedResultMapper;
        List<DefendantAccountEntity> baseAccounts = defendantAccountRepository.findAll(
            OperationReportSpecs.accountFiltersSpec(filters),
            Sort.by(ACCOUNT_NUMBER)
        );
        Set<String> baseAccountNumbers = baseAccounts.stream()
            .map(DefendantAccountEntity::getAccountNumber)
            .collect(Collectors.toSet());

        PaymentReportMode reportMode = filters.getReportMode();
        return switch (reportMode) {
            case SINCE_LAST_ENFORCEMENT -> resultMapper.map(
                applyBaseFilter(
                    defendantAccountRepository.findAccountsWithPaymentMadeAfterLastEnforcementAction(
                        filters.getSinceLastEnforcementAction().name(),
                        Boolean.TRUE.equals(filters.getIsPaymentMade())
                    ),
                    baseAccountNumbers
                )
            );
            case WITH_REGF -> resultMapper.map(
                applyBaseFilter(
                    defendantAccountRepository.findAccountsWithPaymentMadeAfterFirstRegfEnforcement(
                        Boolean.TRUE.equals(filters.getIsPaymentMade())
                    ),
                    baseAccountNumbers
                )
            );
            default -> resultMapper.map(baseAccounts);
        };
    }

    @Override
    public Class<? extends OperationDetailedReport> getStoredReportDataClass(ReportInstanceEntity reportInstance) {
        return OperationDetailedReport.class;
    }

    private List<DefendantAccountEntity> applyBaseFilter(
        List<DefendantAccountEntity> accounts,
        Set<String> baseAccountNumbers
    ) {
        return accounts.stream()
            .filter(account -> baseAccountNumbers.contains(account.getAccountNumber()))
            .toList();
    }

    private OperationReportByPaymentFiltersDto readFilters(ReportInstanceEntity reportInstance) {
        try {
            return objectMapper.readValue(
                reportInstance.getReportParameters(),
                OperationReportByPaymentFiltersDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse report filters", e);
        }
    }

    @Override
    public byte[] convertReportDataToFileType(
        ReportInstanceEntity reportInstance,
        ReportDataInterface reportData,
        FileType fileType
    ) {
        throw new UnsupportedOperationException();
    }
}