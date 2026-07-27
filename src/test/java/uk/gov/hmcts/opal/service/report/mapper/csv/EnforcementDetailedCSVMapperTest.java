package uk.gov.hmcts.opal.service.report.mapper.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.service.report.operation.OperationDetailedReport;

class EnforcementDetailedCSVMapperTest {

    private static final String TRANSACTION_VALUE = "TRANSACTION";
    private static final String DETAIL = "DETAIL";

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

    private static final String TXN_ACCOUNT_NO_1 = "ACCT-1";
    private static final String TXN_CONSOLIDATED_ACCOUNT_NO_1 = "CON-1";
    private static final LocalDate TXN_DATE_1 = LocalDate.of(2026, 6, 11);
    private static final String TXN_TYPE_1 = "PAYMENT";
    private static final String TXN_DETAILS_1 = "Payment for enforcement action";
    private static final String TXN_USER_ID_1 = "user-1";
    private static final BigDecimal TXN_AMOUNT_1 = new BigDecimal("12.34");

    private static final String TXN_ACCOUNT_NO_2 = "ACCT-2";
    private static final String TXN_CONSOLIDATED_ACCOUNT_NO_2 = "CON-2";
    private static final LocalDate TXN_DATE_2 = LocalDate.of(2026, 6, 12);
    private static final String TXN_TYPE_2 = "ADJUSTMENT";
    private static final String TXN_DETAILS_2 = "Adjustment made after review";
    private static final String TXN_USER_ID_2 = "user-2";
    private static final BigDecimal TXN_AMOUNT_2 = new BigDecimal("56.78");

    private final EnforcementDetailedCSVMapper mapper = new EnforcementDetailedCSVMapper();

    @Test
    void reportToCSVString_withNoAccounts_returnsOnlyHeaders() {
        OperationDetailedReport report = reportWithAccounts(List.of());

        String csv = mapper.reportToCSVString(report);

        assertEquals(
            csvRow(
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
            )
                + csvRow(
                "HEADER2",
                "accountno",
                "conacno",
                "txndate",
                "txntype",
                "txndets",
                "txnuser",
                "txnamount"
            ),
            csv
        );
    }

    @Test
    void reportToCSVString_withOneAccountAndNoTransactions_returnsHeadersAndAccountRow() {
        OperationDetailedReport report = reportWithAccounts(List.of(
            accountReport(
                accountRow(),
                List.of()
            )
        ));

        String csv = mapper.reportToCSVString(report);

        assertEquals(
            csvRow(
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
            )
                + csvRow(
                "HEADER2",
                "accountno",
                "conacno",
                "txndate",
                "txntype",
                "txndets",
                "txnuser",
                "txnamount"
            )
                + csvRow(
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
            ),
            csv
        );
    }

    @Test
    void reportToCSVString_withMultipleAccountsAndTransactions_returnsAllRowsInOrder() {
        OperationDetailedReport report = reportWithAccounts(List.of(
            accountReport(
                accountRow(),
                List.of(
                    transactionRow(TXN_ACCOUNT_NO_1, TXN_CONSOLIDATED_ACCOUNT_NO_1, TXN_DATE_1,
                        TXN_TYPE_1, TXN_DETAILS_1, TXN_USER_ID_1, TXN_AMOUNT_1),
                    transactionRow(TXN_ACCOUNT_NO_2, TXN_CONSOLIDATED_ACCOUNT_NO_2, TXN_DATE_2,
                        TXN_TYPE_2, TXN_DETAILS_2, TXN_USER_ID_2, TXN_AMOUNT_2)
                )
            ),
            accountReport(
                accountRow(),
                List.of(transactionRow(TXN_ACCOUNT_NO_2, TXN_CONSOLIDATED_ACCOUNT_NO_2, TXN_DATE_2,
                    TXN_TYPE_2, TXN_DETAILS_2, TXN_USER_ID_2, TXN_AMOUNT_2))
            )
        ));

        String csv = mapper.reportToCSVString(report);

        assertEquals(
            csvRow(
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
            )
                + csvRow(
                "HEADER2",
                "accountno",
                "conacno",
                "txndate",
                "txntype",
                "txndets",
                "txnuser",
                "txnamount"
            )
                + csvRow(
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
            )
                + csvRow(
                TRANSACTION_VALUE,
                TXN_ACCOUNT_NO_1,
                TXN_CONSOLIDATED_ACCOUNT_NO_1,
                TXN_DATE_1.toString(),
                TXN_TYPE_1,
                TXN_DETAILS_1,
                TXN_USER_ID_1,
                TXN_AMOUNT_1.toString()
            )
                + csvRow(
                TRANSACTION_VALUE,
                TXN_ACCOUNT_NO_2,
                TXN_CONSOLIDATED_ACCOUNT_NO_2,
                TXN_DATE_2.toString(),
                TXN_TYPE_2,
                TXN_DETAILS_2,
                TXN_USER_ID_2,
                TXN_AMOUNT_2.toString()
            )
                + csvRow(
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
            )
                + csvRow(
                TRANSACTION_VALUE,
                TXN_ACCOUNT_NO_2,
                TXN_CONSOLIDATED_ACCOUNT_NO_2,
                TXN_DATE_2.toString(),
                TXN_TYPE_2,
                TXN_DETAILS_2,
                TXN_USER_ID_2,
                TXN_AMOUNT_2.toString()
            ),
            csv
        );
    }

    private OperationDetailedReport reportWithAccounts(
        List<DetailedAccountReportDto> accounts) {
        DetailedReportDto detailedReportDto = DetailedReportDto.builder()
            .accountTransactionReports(accounts)
            .build();

        OperationDetailedReport report = new OperationDetailedReport();
        report.setDetailedReport(detailedReportDto);
        return report;
    }

    private DetailedAccountReportDto accountReport(
        DetailedOperationReportAccountRowDto accountRow,
        List<DetailedReportTransactionRowDto> transactionRows) {
        return DetailedAccountReportDto.builder()
            .accountRow(accountRow)
            .transactionRows(transactionRows)
            .build();
    }

    private DetailedOperationReportAccountRowDto accountRow() {
        return DetailedOperationReportAccountRowDto.builder()
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
    }

    private DetailedReportTransactionRowDto transactionRow(
        String accountNo,
        String consolidatedAccountNo,
        LocalDate transactionDate,
        String transactionType,
        String transactionDetails,
        String transactionUserId,
        BigDecimal transactionAmount) {
        return DetailedReportTransactionRowDto.builder()
            .accountNo(accountNo)
            .consolidatedAccountNo(consolidatedAccountNo)
            .transactionDate(transactionDate)
            .transactionType(transactionType)
            .transactionDetails(transactionDetails)
            .transactionUserId(transactionUserId)
            .transactionAmount(transactionAmount)
            .build();
    }

    private String csvRow(String... cells) {
        return String.join(",", cells) + "\n";
    }
}
