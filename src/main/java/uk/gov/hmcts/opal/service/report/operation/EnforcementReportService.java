package uk.gov.hmcts.opal.service.report.operation;

import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_NUMBER;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.ALL;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_ENFORCEMENT;
import static uk.gov.hmcts.opal.service.report.ReportType.SUMMARY;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.exception.UnsupportedContentTypeException;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs;
import uk.gov.hmcts.opal.repository.jpa.OperationReportSpecs;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.ReportCSVService;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportId;
import uk.gov.hmcts.opal.service.report.ReportInterface;
import uk.gov.hmcts.opal.service.report.operation.mapper.DetailedResultMapper;
import uk.gov.hmcts.opal.service.report.operation.mapper.SummaryResultMapper;

@Service
@RequiredArgsConstructor
public class EnforcementReportService implements ReportInterface<OperationReportDataInterface> {

    private final DefendantAccountRepository defendantAccountRepository;
    private final EnforcementRepository enforcementRepository;
    private final SummaryResultMapper summaryResultMapper;
    private final DetailedResultMapper detailedResultMapper;
    private final ObjectMapper objectMapper;
    private final EnforcementReportValidator validator;
    private final ReportCSVService reportCSVService;

    @Override
    public ReportId getReportId() {
        return OP_ENFORCEMENT;
    }

    @Override
    public OperationReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {

        OperationReportByEnforcementFiltersDto filters = readFilters(reportInstance);
        validator.validate(filters);
        Specification<DefendantAccountEntity> accountSpec = OperationReportSpecs.accountFiltersSpec(filters);
        List<DefendantAccountEntity> accounts;

        if (isNotFilteringOnEnforcementData(filters)) {
            accounts = defendantAccountRepository.findAll(accountSpec, Sort.by(ACCOUNT_NUMBER));
        } else {
            Specification<EnforcementEntity> enforcementSpec = EnforcementReportSpecs.build(filters);
            List<EnforcementEntity> enforcements = enforcementRepository.findAll(enforcementSpec);
            List<Long> accountIds = enforcements.stream()
                .map(EnforcementEntity::getDefendantAccount)
                .map(DefendantAccountEntity::getDefendantAccountId)
                .distinct()
                .toList();
            accounts = defendantAccountRepository.findAll(
                accountSpec.and(OperationReportSpecs.defendantAccountIdsIn(accountIds)),
                Sort.by(DefendantAccountEntity_.ACCOUNT_NUMBER));
        }
        return filters.getReportType() == SUMMARY
            ? summaryResultMapper.map(accounts)
            : detailedResultMapper.map(accounts);
    }

    @Override
    public Class<? extends OperationReportDataInterface> getStoredReportDataClass(
        ReportInstanceEntity reportInstance) {
        OperationReportByEnforcementFiltersDto filters = readFilters(reportInstance);
        return filters.getReportType() == SUMMARY
            ? OperationSummaryReport.class
            : OperationDetailedReport.class;
    }

    private static boolean isNotFilteringOnEnforcementData(OperationReportByEnforcementFiltersDto filters) {
        ReportEnforcementMode enforcementMode =
            Optional.ofNullable(filters.getReportEnforcementMode()).orElse(ReportEnforcementMode.ALL);
        return enforcementMode.equals(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT)
            || (enforcementMode.equals(ALL) && filters.getEnforcementDateTo() == null
            && filters.getEnforcementDateFrom() == null);
    }

    private OperationReportByEnforcementFiltersDto readFilters(ReportInstanceEntity reportInstance) {
        try {
            return objectMapper.readValue(
                reportInstance.getReportParameters(),
                OperationReportByEnforcementFiltersDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse report filters", e);
        }
    }

    @Override
    public byte[] convertReportDataToFileType(
        ReportInstanceEntity reportInstance,
        OperationReportDataInterface reportData,
        FileType fileType) {
        if (fileType != FileType.CSV) {
            throw new UnsupportedContentTypeException(
                getReportId().name(), fileType.name(), List.of(FileType.CSV.name()));
        }
        if (!(reportData instanceof OperationSummaryReport summaryReport)) {
            throw new UnsupportedContentTypeException(getReportId().name(), "DETAILED CSV", List.of("SUMMARY CSV"));
        }
        return reportCSVService.convertReportDtoToCSV(summaryReport);
    }
}
