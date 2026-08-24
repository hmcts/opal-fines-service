package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatReportMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatReportValue;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.service.report.operation.OperationSummaryReport;

@Component
public class SummaryReportCSVMapper implements ReportCSVMapper<OperationSummaryReport> {

    private static final String REPORT_NAME = "Operation summary report";

    @Override
    public Class<OperationSummaryReport> getReportDataType() {
        return OperationSummaryReport.class;
    }

    @Override
    public String reportToCSVString(OperationSummaryReport report) {
        SummaryReportDto summaryReport = summaryReport(report);
        List<SummaryOperationReportRowDto> rows = Optional.ofNullable(summaryReport.getReportSummaryRows())
            .orElse(List.of());

        StringBuilder csv = new StringBuilder()
            .append(dataListToFullCSVRow(header1Row()));
        for (SummaryOperationReportRowDto row : rows) {
            csv.append(dataListToFullCSVRow(detailRow(row)));
        }
        return csv.append(dataListToFullCSVRow(header2Row()))
            .append(dataListToFullCSVRow(summaryRow(summaryReport.getTotals())))
            .toString();
    }

    private SummaryReportDto summaryReport(OperationSummaryReport report) {
        if (report == null || report.getSummaryReport() == null) {
            throw new UnprocessableException(REPORT_NAME + " data is required.");
        }
        if (report.getSummaryReport().getTotals() == null) {
            throw new UnprocessableException(REPORT_NAME + " totals are required.");
        }
        return report.getSummaryReport();
    }

    private List<String> header1Row() {
        return List.of(
            "HEADER1",
            "company",
            "defname",
            "accountno",
            "dob",
            "nino",
            "co",
            "dlmove",
            "imposed",
            "paidsf",
            "balance",
            "address1",
            "address2",
            "address3",
            "address4",
            "address5",
            "postcode",
            "imposingcourt",
            "mobtel",
            "hometel",
            "bustel",
            "vehiclereg",
            "vehiclemake",
            "email-1",
            "email-2",
            "empref",
            "empname",
            "empadd1",
            "empadd2",
            "empadd3",
            "empadd4",
            "empadd5",
            "emppcode",
            "emptel",
            "empemail",
            "lastenf",
            "edrdate",
            "enfreason",
            "ledate",
            "user",
            "enfcrt",
            "warrno",
            "did",
            "pg",
            "pcr");
    }

    private List<String> detailRow(SummaryOperationReportRowDto row) {
        return List.of(
            formatReportValue(row.getHeader1()),
            formatReportValue(row.getCompany()),
            formatReportValue(row.getDefendantName()),
            formatReportValue(row.getAccountNo()),
            formatReportValue(row.getDateOfBirth()),
            formatReportValue(row.getNationalInsuranceNo()),
            formatReportValue(row.getCollectionOrder()),
            formatReportValue(row.getLastMovementDate()),
            formatReportMoney(row.getAmountImposed()),
            formatReportMoney(row.getAmountPaid()),
            formatReportMoney(row.getBalance()),
            formatReportValue(row.getAddress1()),
            formatReportValue(row.getAddress2()),
            formatReportValue(row.getAddress3()),
            EMPTY_STRING,
            EMPTY_STRING,
            formatReportValue(row.getPostcode()),
            formatReportValue(row.getImposingCourt()),
            formatReportValue(row.getMobTel()),
            formatReportValue(row.getHomeTel()),
            formatReportValue(row.getBusinessTel()),
            formatReportValue(row.getVehicleReg()),
            formatReportValue(row.getVehicleMake()),
            formatReportValue(row.getEmail1()),
            formatReportValue(row.getEmail2()),
            formatReportValue(row.getEmployeeRef()),
            formatReportValue(row.getEmployerName()),
            formatReportValue(row.getEmployerAddress1()),
            formatReportValue(row.getEmployerAddress2()),
            formatReportValue(row.getEmployerAddress3()),
            formatReportValue(row.getEmployerAddress4()),
            formatReportValue(row.getEmployerAddress5()),
            formatReportValue(row.getEmployerPostcode()),
            formatReportValue(row.getEmployerTel()),
            formatReportValue(row.getEmployerEmail()),
            formatReportValue(row.getLastEnforcement()),
            formatReportValue(row.getLastEnforcementDate()),
            formatReportValue(row.getEnforcementReason()),
            formatReportValue(row.getEarliestReleaseDate()),
            formatReportValue(row.getUser()),
            formatReportValue(row.getEnforcingCourtCode()),
            formatReportValue(row.getWarrantRef()),
            formatReportValue(row.getJailDays()),
            formatReportValue(row.getParentOrGuardian()),
            formatReportValue(row.getProsecutorCaseReference()));
    }

    private List<String> header2Row() {
        return List.of(
            "HEADER2",
            "Accounts Reported",
            "Total Impositions",
            "Total Paid",
            "Total Balance");
    }

    private List<String> summaryRow(SummaryReportTotalsRowDto totals) {
        return List.of(
            "SUMMARY",
            formatReportValue(totals.getAccountsReported()),
            formatReportMoney(totals.getTotalImposed()),
            formatReportMoney(totals.getTotalPaid()),
            formatReportMoney(totals.getTotalBalance()));
    }
}
