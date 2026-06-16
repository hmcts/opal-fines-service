package uk.gov.hmcts.opal.service.report.operation;

import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_NUMBER;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_PAYMENT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.OperationReportSpecs;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportId;
import uk.gov.hmcts.opal.service.report.ReportInterface;
import uk.gov.hmcts.opal.service.report.operation.mapper.DetailedResultMapper;

@Service
@RequiredArgsConstructor
public class OperationReportByPaymentService implements ReportInterface {

    private final DefendantAccountRepository defendantAccountRepository;
    private final DefendantTransactionRepository defendantTransactionRepository;
    private final EnforcementRepository enforcementRepository;
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
        List<DefendantAccountEntity> accounts = defendantAccountRepository.findAll(
            OperationReportSpecs.accountFiltersSpec(filters),
            Sort.by(ACCOUNT_NUMBER)
        );
        if (filters.getSinceLastEnforcementAction() != null) {
            return detailedResultMapper.map(filterByLatestEnforcementPayment(accounts, filters));
        }
        if (Boolean.TRUE.equals(filters.getIsWithRegf())) {
            return detailedResultMapper.map(filterByRegfPayment(accounts, filters));
        }
        return detailedResultMapper.map(accounts);
    }

    private List<DefendantAccountEntity> filterByLatestEnforcementPayment(
        List<DefendantAccountEntity> accounts,
        OperationReportByPaymentFiltersDto filters
    ) {
        boolean paymentMade = Boolean.TRUE.equals(filters.getIsPaymentMade());
        return accounts.stream()
            .filter(account -> paymentMade == hasPaymentAfterLatestEnforcement(
                account, filters.getSinceLastEnforcementAction()))
            .toList();
    }

    private List<DefendantAccountEntity> filterByRegfPayment(
        List<DefendantAccountEntity> accounts,
        OperationReportByPaymentFiltersDto filters
    ) {
        boolean paymentMade = Boolean.TRUE.equals(filters.getIsPaymentMade());
        return accounts.stream()
            .filter(account -> {
                EnforcementEntity enforcement =
                    enforcementRepository.findTopByDefendantAccountIdAndResultIdOrderByPostedDateAsc(
                        account.getDefendantAccountId(),
                        ResultId.REGF.name()
                    );
                if (enforcement == null || enforcement.getPostedDate() == null) {
                    return false;
                }
                boolean hasPaymentAfterRegf =
                    defendantTransactionRepository.existsByDefendantAccountIdAndPostedDateGreaterThanEqual(
                        account.getDefendantAccountId(),
                        enforcement.getPostedDate().toLocalDate()
                    );
                return paymentMade == hasPaymentAfterRegf;
            })
            .toList();
    }

    private boolean hasPaymentAfterLatestEnforcement(
        DefendantAccountEntity account,
        ResultId enforcementAction
    ) {
        EnforcementEntity enforcement =
            enforcementRepository.findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
                account.getDefendantAccountId(),
                enforcementAction.value()
            );
        return hasPaymentAfterEnforcement(account, enforcement);
    }

    private boolean hasPaymentAfterEnforcement(
        DefendantAccountEntity account,
        EnforcementEntity enforcement
    ) {
        if (enforcement == null || enforcement.getPostedDate() == null) {
            return false;
        }
        return defendantTransactionRepository.existsByDefendantAccountIdAndPostedDateGreaterThanEqual(
            account.getDefendantAccountId(),
            enforcement.getPostedDate().toLocalDate()
        );
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