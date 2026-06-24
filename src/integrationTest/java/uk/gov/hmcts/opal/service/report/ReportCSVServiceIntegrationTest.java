package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementDetailedReport;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

class ReportCSVServiceIntegrationTest extends AbstractIntegrationTest {

    private static final String DETAIL = "DETAIL";
    private static final String TRANSACTION = "TRANSACTION";

    private static final String COMPANY = "Y";
    private static final String ACCOUNT_NO = "ACCT-1";
    private static final String DEFENDANT_NAME = "Defendant Name";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1980, 1, 2);
    private static final String ADDRESS1 = "Line 1";
    private static final String ADDRESS2 = "Line 2";
    private static final String ADDRESS3 = "Line 3";
    private static final String POSTCODE = "AB1 2CD";
    private static final String EMPLOYEE_REF = "EMP-REF";
    private static final String EMPLOYER_NAME = "Employer Name";
    private static final String EMPLOYER_ADDRESS1 = "Emp Addr 1";
    private static final String EMPLOYER_ADDRESS2 = "Emp Addr 2";
    private static final String EMPLOYER_ADDRESS3 = "Emp Addr 3";
    private static final String EMPLOYER_ADDRESS4 = "Emp Addr 4";
    private static final String EMPLOYER_ADDRESS5 = "Emp Addr 5";
    private static final String EMPLOYER_POSTCODE = "EM1 2PL";
    private static final String EMPLOYER_TEL = "01234567890";
    private static final String EMPLOYER_EMAIL = "employer@example.com";
    private static final String COLLECTION_ORDER = "Y";
    private static final LocalDate LAST_MOVEMENT_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate DATE_OF_HEARING = LocalDate.of(2026, 6, 10);
    private static final String IMPOSING_COURT = "Court 1";
    private static final String PAYMENT_TERMS = "Monthly";
    private static final BigDecimal AMOUNT_IMPOSED = new BigDecimal("100.10");
    private static final BigDecimal BALANCE = new BigDecimal("90.10");
    private static final BigDecimal ARREARS_TOTAL = new BigDecimal("80.10");
    private static final BigDecimal FINE_IMPOSITIONS = new BigDecimal("70.10");
    private static final BigDecimal COST_IMPOSITIONS = new BigDecimal("60.10");
    private static final BigDecimal COMPENSATION_IMPOSITIONS = new BigDecimal("50.10");
    private static final BigDecimal CRIMINAL_COURTS_CHARGE_IMPOSITIONS = new BigDecimal("40.10");
    private static final BigDecimal VICTIM_SURCHARGE_IMPOSITIONS = new BigDecimal("30.10");
    private static final BigDecimal OTHER_IMPOSITIONS = new BigDecimal("20.10");
    private static final String PROSECUTOR_CASE_REFERENCE = "PCR-1";

    private static final String TXN_ACCOUNT_NO = "ACCT-1";
    private static final String TXN_CONSOLIDATED_ACCOUNT_NO = "CON-1";
    private static final LocalDate TXN_DATE = LocalDate.of(2026, 6, 11);
    private static final String TXN_TYPE = "PAYMENT";
    private static final String TXN_USER_ID = "user-1";
    private static final BigDecimal TXN_AMOUNT = new BigDecimal("12.34");

    @Autowired
    private ReportCSVService reportCSVService;

    @Test
    @JiraStory("PO-2283")
    @JiraEpic("PO-2248")
    void convertReportDtoToCSV_happyPath_usesOperationByEnforcementDetailedReportMapper() {
        OperationByEnforcementDetailedReport report = report();

        byte[] result = reportCSVService.convertReportDtoToCSV(report);

        assertArrayEquals(expectedCsv().getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    @JiraStory("PO-2283")
    @JiraEpic("PO-2248")
    void convertReportDtoToCSV_unmappedReportType_throwsUnprocessableException() {
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> reportCSVService.convertReportDtoToCSV(new MissingReportData()));

        assertEquals("Report cannot be converted to CSV format.", exception.getDetailedReason());
    }

    private OperationByEnforcementDetailedReport report() {
        OperationByEnforcementDetailedReportAccountRowDto accountRow =
            OperationByEnforcementDetailedReportAccountRowDto.builder()
                .header1(DETAIL)
                .company(COMPANY)
                .accountNo(ACCOUNT_NO)
                .defendantName(DEFENDANT_NAME)
                .dateOfBirth(DATE_OF_BIRTH)
                .address1(ADDRESS1)
                .address2(ADDRESS2)
                .address3(ADDRESS3)
                .postcode(POSTCODE)
                .employeeRef(EMPLOYEE_REF)
                .employerName(EMPLOYER_NAME)
                .employerAddress1(EMPLOYER_ADDRESS1)
                .employerAddress2(EMPLOYER_ADDRESS2)
                .employerAddress3(EMPLOYER_ADDRESS3)
                .employerAddress4(EMPLOYER_ADDRESS4)
                .employerAddress5(EMPLOYER_ADDRESS5)
                .employerPostcode(EMPLOYER_POSTCODE)
                .employerTel(EMPLOYER_TEL)
                .employerEmail(EMPLOYER_EMAIL)
                .collectionOrder(COLLECTION_ORDER)
                .lastMovementDate(LAST_MOVEMENT_DATE)
                .dateOfHearing(DATE_OF_HEARING)
                .imposingCourt(IMPOSING_COURT)
                .paymentTerms(PAYMENT_TERMS)
                .amountImposed(AMOUNT_IMPOSED)
                .balance(BALANCE)
                .arrearsTotal(ARREARS_TOTAL)
                .fineImpositions(FINE_IMPOSITIONS)
                .costImpositions(COST_IMPOSITIONS)
                .compensationImpositions(COMPENSATION_IMPOSITIONS)
                .criminalCourtsChargeImpositions(CRIMINAL_COURTS_CHARGE_IMPOSITIONS)
                .victimSurchargeImpositions(VICTIM_SURCHARGE_IMPOSITIONS)
                .otherImpositions(OTHER_IMPOSITIONS)
                .prosecutorCaseReference(PROSECUTOR_CASE_REFERENCE)
                .build();

        OperationByEnforcementDetailedReportTransactionRowDto transactionRow =
            OperationByEnforcementDetailedReportTransactionRowDto.builder()
                .accountNo(TXN_ACCOUNT_NO)
                .consolidatedAccountNo(TXN_CONSOLIDATED_ACCOUNT_NO)
                .transactionDate(TXN_DATE)
                .transactionType(TXN_TYPE)
                .transactionUserId(TXN_USER_ID)
                .transactionAmount(TXN_AMOUNT)
                .build();

        OperationByEnforcementDetailedAccountReportDto accountReport =
            OperationByEnforcementDetailedAccountReportDto.builder()
                .accountRow(accountRow)
                .transactionRows(List.of(transactionRow))
                .build();

        OperationByEnforcementDetailedReportDto reportDto = OperationByEnforcementDetailedReportDto.builder()
            .accountTransactionReports(List.of(accountReport))
            .build();

        OperationByEnforcementDetailedReport report = new OperationByEnforcementDetailedReport();
        report.setEnforcementReport(reportDto);
        return report;
    }

    private String expectedCsv() {
        return csvRow(
            "HEADER1",
            "company",
            "accountno",
            "defname",
            "dob",
            "address1",
            "address2",
            "address3",
            "postcode",
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
            "co",
            "dlmove",
            "dateofhearing",
            "imposingcourt",
            "paymentterms",
            "imposed",
            "balance",
            "arrears",
            "fines",
            "costs",
            "comps",
            "fcc",
            "fvs",
            "other",
            "pcr"
        ) + csvRow(
            "HEADER2",
            "accountno",
            "conacno",
            "txndate",
            "txntype",
            "txndets",
            "txnuser",
            "txnamount"
        ) + csvRow(
            DETAIL,
            COMPANY,
            ACCOUNT_NO,
            DEFENDANT_NAME,
            DATE_OF_BIRTH.toString(),
            ADDRESS1,
            ADDRESS2,
            ADDRESS3,
            POSTCODE,
            EMPLOYEE_REF,
            EMPLOYER_NAME,
            EMPLOYER_ADDRESS1,
            EMPLOYER_ADDRESS2,
            EMPLOYER_ADDRESS3,
            EMPLOYER_ADDRESS4,
            EMPLOYER_ADDRESS5,
            EMPLOYER_POSTCODE,
            EMPLOYER_TEL,
            EMPLOYER_EMAIL,
            COLLECTION_ORDER,
            LAST_MOVEMENT_DATE.toString(),
            DATE_OF_HEARING.toString(),
            IMPOSING_COURT,
            PAYMENT_TERMS,
            AMOUNT_IMPOSED.toString(),
            BALANCE.toString(),
            ARREARS_TOTAL.toString(),
            FINE_IMPOSITIONS.toString(),
            COST_IMPOSITIONS.toString(),
            COMPENSATION_IMPOSITIONS.toString(),
            CRIMINAL_COURTS_CHARGE_IMPOSITIONS.toString(),
            VICTIM_SURCHARGE_IMPOSITIONS.toString(),
            OTHER_IMPOSITIONS.toString(),
            PROSECUTOR_CASE_REFERENCE
        ) + csvRow(
            TRANSACTION,
            TXN_ACCOUNT_NO,
            TXN_CONSOLIDATED_ACCOUNT_NO,
            TXN_DATE.toString(),
            TXN_TYPE,
            "",
            TXN_USER_ID,
            TXN_AMOUNT.toString()
        );
    }

    private String csvRow(String... cells) {
        return String.join(",", cells) + "\n";
    }

    private static final class MissingReportData implements ReportDataInterface {
        @Override
        public long getNumberOfRecords() {
            return 0;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return null;
        }
    }
}
