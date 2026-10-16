package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.escapeCsv;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatReportDate;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateRequired;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateText;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;
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

    private static final String REPORT_NAME = "Cash Till report";
    private static final Sort PAYMENT_DATE_DESC = Sort.by(Sort.Direction.DESC, PaymentInEntity_.PAYMENT_DATE);
    private static final List<String> REPORT_HEADINGS = List.of(
        "HEADER1", CashTillReportField.REPORT_TYPE.label(), CashTillReportField.TILL_NUMBER.label(),
        CashTillReportField.SOURCE.label(), CashTillReportField.REPORT_AMOUNT.label(),
        CashTillReportField.CASHIER.label(), CashTillReportField.REPORT_CREATED.label(),
        CashTillReportField.TIME_CREATED.label(), CashTillReportField.BUSINESS_UNIT.label(),
        CashTillReportField.NUMBER_OF_RECORDS.label(), CashTillReportField.CREATED_BY.label()
    );
    private static final List<String> DETAIL_HEADINGS = List.of(
        "HEADER2", CashTillDetailField.DATE.label(), CashTillDetailField.TRANSACTION_TYPE.label(),
        CashTillDetailField.DETAILS.label(), CashTillDetailField.METHOD_OF_PAYMENT.label(),
        CashTillDetailField.TRANSACTION_AMOUNT.label()
    );
    private static final List<String> SUMMARY_HEADINGS = List.of(
        "HEADER3", CashTillSummaryField.NUMBER_OF_RECORDS.label(), CashTillSummaryField.TOTAL_AMOUNT.label()
    );
    private static final String AUTO_CASH_INPUT_SUFFIX = " - Auto cash input";
    private static final String ALLOCATED_PAYMENT_PREFIX = "*";
    private static final String CASH_TILL_REPORT_PARAMETERS_ERROR = "Failed to parse Cash Till report parameters";
    private static final DateTimeFormatter REPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String REPORT_TYPE_ALLOCATED = "Allocated";
    private static final String REPORT_TYPE_PREALLOCATED = "Pre-allocated";

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
        if (fileType != CSV) {
            throw new IllegalArgumentException("Cash Till report only supports CSV conversion");
        }

        String csv = "";
        validateReportData(reportData);
        StringBuilder reportCSV = new StringBuilder(String.join(COMMA, REPORT_HEADINGS)).append(NEW_LINE);
        reportCSV.append(toReportCsvRow(reportInstance, reportData)).append(NEW_LINE);
        csv += reportCSV.append(NEW_LINE);

        StringBuilder detailsCSV = new StringBuilder(String.join(COMMA, DETAIL_HEADINGS)).append(NEW_LINE);
        reportData.getRows().stream()
            .sorted(Comparator.comparing(CashTillReportRow::getPaymentDateTime).reversed())
            .map(row -> toDetailCsvRow(reportData, row))
            .forEach(row -> detailsCSV.append(row).append(NEW_LINE));
        csv += detailsCSV.append(NEW_LINE);

        StringBuilder summaryCSV = new StringBuilder(String.join(COMMA, SUMMARY_HEADINGS)).append(NEW_LINE);
        summaryCSV.append(toSummaryCsvRow(reportData));
        csv += summaryCSV;


        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private static String toDetailCsvRow(CashTillReportData reportData, CashTillReportRow row) {
        return String.join(COMMA, List.of(
            "DETAIL",
            escapeCsv(formatReportDate(row.getPaymentDateTime())),
            escapeCsv(row.getDestinationType().toDisplayName()),
            escapeCsv(formatDetails(reportData, row)),
            escapeCsv(row.getPaymentMethod().toDisplayName()),
            escapeCsv(formatMoney(row.getAmount()))
        ));
    }

    private static String toReportCsvRow(ReportInstanceEntity reportInstance, CashTillReportData reportData) {
        return String.join(COMMA, List.of(
            "REPORT",
            escapeCsv(reportData.getAllocatedReport() ? REPORT_TYPE_ALLOCATED : REPORT_TYPE_PREALLOCATED),
            escapeCsv(reportData.getCashTillNumber()),
            escapeCsv(reportData.getSource().name()),
            escapeCsv(formatMoney(reportData.getTotalAmount())),
            escapeCsv(reportData.getCashier()),
            escapeCsv(formatReportDate(reportInstance.getCreatedTimestamp())),
            escapeCsv(formatReportTime(reportInstance.getCreatedTimestamp())),
            escapeCsv(reportData.getBusinessUnit()),
            escapeCsv(String.valueOf(reportData.getRows().size())),
            escapeCsv(reportInstance.getRequestedByName())
        ));
    }

    private static String toSummaryCsvRow(CashTillReportData reportData) {
        return String.join(COMMA, List.of(
            "SUMMARY",
            escapeCsv(String.valueOf(reportData.getRows().size())),
            escapeCsv(reportData.getTotalAmount().toString())
        ));
    }

    private static String formatDetails(CashTillReportData reportData, CashTillReportRow row) {
        String details = row.getDetails();
        if (Boolean.TRUE.equals(reportData.getAllocatedReport()) && Boolean.TRUE.equals(row.getAllocated())) {
            details = ALLOCATED_PAYMENT_PREFIX + details;
        }
        if (Boolean.TRUE.equals(row.getAutoPayment())) {
            details += AUTO_CASH_INPUT_SUFFIX;
        }
        return details;
    }

    public static String formatReportTime(TemporalAccessor value) {
        return Optional.ofNullable(value)
            .map(REPORT_TIME_FORMATTER::format)
            .orElse(EMPTY_STRING);
    }

    private static void validateReportData(CashTillReportData reportData) {
        if (reportData == null) {
            throw new IllegalArgumentException("Cash Till report data is required");
        }
        if (reportData.getRows() == null) {
            throw new IllegalArgumentException("Cash Till report rows are required");
        }
        for (int rowIndex = 0; rowIndex < reportData.getRows().size(); rowIndex++) {
            validateRow(reportData.getRows().get(rowIndex), rowIndex + 1);
        }
    }

    private static void validateRow(CashTillReportRow row, int rowNumber) {
        if (row == null) {
            throw new IllegalArgumentException("Cash Till report row " + rowNumber + " is required");
        }

        validateText(row.getDetails(), CashTillDetailField.DETAILS.label(), REPORT_NAME, rowNumber);
        validateRequired(row.getDestinationType(),
            CashTillDetailField.TRANSACTION_TYPE.label(), REPORT_NAME, rowNumber);
        validateRequired(row.getPaymentMethod(), CashTillDetailField.METHOD_OF_PAYMENT.label(), REPORT_NAME, rowNumber);
        validateDate(row, rowNumber);
        validateRequired(row.getAutoPayment(), "auto_payment", REPORT_NAME, rowNumber);
        validateMoney(row.getAmount(), CashTillDetailField.TRANSACTION_AMOUNT.label(), REPORT_NAME, rowNumber);
    }

    private static void validateDate(CashTillReportRow row, int rowNumber) {
        if (row.getPaymentDateTime() == null) {
            throw new IllegalArgumentException(
                CashTillDetailField.DATE.label() + " is required at Cash Till report row " + rowNumber);
        }
    }

    private enum CashTillReportField {
        REPORT_TYPE("report_type"),
        TILL_NUMBER("till_number"),
        SOURCE("source"),
        REPORT_AMOUNT("report_amount"),
        CASHIER("cashier"),
        REPORT_CREATED("report_created"),
        TIME_CREATED("time_created"),
        BUSINESS_UNIT("business_unit"),
        NUMBER_OF_RECORDS("no_of_records"),
        CREATED_BY("created_by");

        private final String label;

        CashTillReportField(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }

    private enum CashTillDetailField {
        DATE("date"),
        TRANSACTION_TYPE("transaction_type"),
        DETAILS("details"),
        METHOD_OF_PAYMENT("method_of_payment"),
        TRANSACTION_AMOUNT("transaction_amount");

        private final String label;

        CashTillDetailField(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }

    private enum CashTillSummaryField {
        NUMBER_OF_RECORDS("no_of_records"),
        TOTAL_AMOUNT("total_amount");

        private final String label;

        CashTillSummaryField(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
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
