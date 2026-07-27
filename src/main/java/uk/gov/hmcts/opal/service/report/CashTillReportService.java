package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity_;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.repository.jpa.PaymentInSpecs;

@Service
@RequiredArgsConstructor
public class CashTillReportService implements ReportInterface<CashTillReportData> {

    private static final Sort PAYMENT_DATE_DESC = Sort.by(Sort.Direction.DESC, PaymentInEntity_.PAYMENT_DATE);
    private static final String CASH_TILL_REPORT_PARAMETERS_ERROR = "Failed to parse Cash Till report parameters";

    private final ObjectMapper objectMapper;
    private final TillRepository tillRepository;
    private final PaymentInRepository paymentInRepository;
    private final CashTillReportDataMapper reportDataMapper;

    @Override
    public ReportId getReportId() {
        return CASH_TILL;
    }

    @Override
    public CashTillReportData generateReportData(ReportInstanceEntity reportInstance) {
        CashTillReportParameters parameters = readParameters(reportInstance);
        long tillId = requireTillId(parameters);
        TillEntity till = tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Cash Till report till not found for till_id " + tillId));
        List<PaymentInEntity> payments = paymentInRepository.findAll(
            paymentSearch(parameters, tillId),
            PAYMENT_DATE_DESC);
        return reportDataMapper.map(parameters.isAllocatedReport(), till, payments);
    }

    @Override
    public Class<? extends CashTillReportData> getStoredReportDataClass(ReportInstanceEntity reportInstance) {
        return CashTillReportData.class;
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, CashTillReportData reportData,
        FileType fileType) {
        throw new IllegalArgumentException("Cash Till report only supports CSV conversion");
    }

    private static Specification<PaymentInEntity> paymentSearch(CashTillReportParameters parameters, long tillId) {
        return PaymentInSpecs.equalsTillId(tillId);
    }

    private CashTillReportParameters readParameters(ReportInstanceEntity reportInstance) {
        if (reportInstance == null) {
            throw new IllegalArgumentException("Cash Till report instance is required");
        }
        try {
            return objectMapper.readValue(reportInstance.getReportParameters(), CashTillReportParameters.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(CASH_TILL_REPORT_PARAMETERS_ERROR, e);
        }
    }

    private static long requireTillId(CashTillReportParameters parameters) {
        return Optional.ofNullable(parameters.tillId())
            .filter(tillId -> tillId > 0)
            .orElseThrow(() -> new IllegalArgumentException("Cash Till report till_id is required"));
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private record CashTillReportParameters(Long tillId, Boolean allocatedReport) {

        private boolean isAllocatedReport() {
            return Boolean.TRUE.equals(allocatedReport);
        }
    }
}
