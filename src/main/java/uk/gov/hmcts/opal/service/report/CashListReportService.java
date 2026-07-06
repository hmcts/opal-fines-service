package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.CASH_LIST;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

@Service
@RequiredArgsConstructor
public class CashListReportService implements ReportInterface<CashListReportData> {

    private static final String TILL_ID = "till_id";
    private static final String MISSING_TILL_ID_MESSAGE = "Cash List report requires a till_id report parameter";
    private static final String INVALID_TILL_ID_MESSAGE = "Cash List report parameter till_id must be a whole number";

    private final ObjectMapper objectMapper;
    private final TillRepository tillRepository;
    private final PaymentInRepository paymentInRepository;
    private final CashListReportAssembler cashListReportAssembler;

    @Override
    public ReportId getReportId() {
        return CASH_LIST;
    }

    @Override
    @Transactional(readOnly = true)
    public CashListReportData generateReportData(ReportInstanceEntity reportInstance) {
        Long tillId = extractTillId(readCashListReportParameters(reportInstance).tillId());
        TillEntity till = getTill(tillId);
        BusinessUnitEntity businessUnit = getBusinessUnit(till, tillId);
        List<PaymentInEntity> payments =
            paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(tillId);

        return cashListReportAssembler.toReportData(till, businessUnit, payments);
    }

    @Override
    public Class<? extends CashListReportData> getStoredReportDataClass(ReportInstanceEntity reportInstance) {
        return CashListReportData.class;
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, CashListReportData reportData,
        FileType fileType) {
        return new byte[0];
    }

    private static long extractTillId(String tillIdParameter) {
        if (tillIdParameter == null) {
            throw new IllegalArgumentException(MISSING_TILL_ID_MESSAGE);
        }

        long tillId;
        try {
            tillId = Long.parseLong(tillIdParameter);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_TILL_ID_MESSAGE, e);
        }

        if (tillId <= 0) {
            throw new IllegalArgumentException("Cash List report parameter till_id must be greater than zero");
        }
        return tillId;
    }

    private TillEntity getTill(Long tillId) {
        return tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Cash List report till not found for till_id: " + tillId));
    }

    private static BusinessUnitEntity getBusinessUnit(TillEntity till, Long tillId) {
        BusinessUnitEntity businessUnit = till.getBusinessUnit();
        if (businessUnit == null) {
            throw new EntityNotFoundException("Cash List report business unit not found for till_id: " + tillId);
        }
        return businessUnit;
    }

    private CashListReportParameters readCashListReportParameters(ReportInstanceEntity reportInstance) {
        String reportParameters = reportInstance.getReportParameters();
        if (reportParameters == null || reportParameters.isBlank()) {
            throw new IllegalArgumentException(MISSING_TILL_ID_MESSAGE);
        }

        try {
            return objectMapper.readValue(reportParameters, CashListReportParameters.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cash List report parameters must be valid JSON", e);
        }
    }

    private record CashListReportParameters(@JsonProperty(TILL_ID) String tillId) {

    }
}
