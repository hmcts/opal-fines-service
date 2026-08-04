package uk.gov.hmcts.opal.service.report.mapper.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.service.report.operation.OperationSummaryReport;

class SummaryReportCSVMapperTest {

    private final SummaryReportCSVMapper mapper = new SummaryReportCSVMapper();

    @Test
    void reportToCSVString_returnsF133SummaryCsv() {
        OperationSummaryReport report = OperationSummaryReport.builder()
            .summaryReport(SummaryReportDto.builder()
                .reportSummaryRows(List.of(summaryRow()))
                .totals(SummaryReportTotalsRowDto.builder()
                    .accountsReported(1)
                    .totalImposed(new BigDecimal("250.00"))
                    .totalPaid(new BigDecimal("65.00"))
                    .totalBalance(new BigDecimal("185.00"))
                    .build())
                .build())
            .build();

        String[] lines = mapper.reportToCSVString(report).split("\n", -1);

        assertThat(lines).containsExactly(
            "HEADER1,company,defname,accountno,dob,nino,co,dlmove,imposed,paidsf,balance,address1,address2,"
                + "address3,address4,address5,postcode,imposingcourt,mobtel,hometel,bustel,vehiclereg,vehiclemake,"
                + "email-1,email-2,empref,empname,empadd1,empadd2,empadd3,empadd4,empadd5,emppcode,emptel,"
                + "empemail,lastenf,edrdate,enfreason,ledate,user,enfcrt,warrno,did,pg,pcr",
            "DETAIL,N,\"Jones, Michael \"\"Mick\"\"\",05000123E,10/10/1970,AA123456A,Y,06/02/2006,250.00,"
                + "65.00,185.00,85 High Street,Colchester,Essex,,,CO8 6RR,Basildon Magistrates Court,"
                + "07986-554423,01255-789333,01345-0123456,AC 51 GHF,Ford Focus,"
                + "michaelianjones@example.com,mickey@example.com,JONES25684E,BigCo Ltd,BigCo House,"
                + "55 High Street,Springfield,Chelmsford,Essex,CM7 6TT,01245-000000,info@bigco.co.uk,MAN,"
                + "06/02/2007,Personal circumstances,07/02/2007,NT,100,100/16/00003,10,N,01TRT884424L",
            "HEADER2,Accounts Reported,Total Impositions,Total Paid,Total Balance",
            "SUMMARY,1,250.00,65.00,185.00",
            "");
    }

    @Test
    void reportToCSVString_missingReportData_throwsUnprocessableException() {
        assertThatThrownBy(() -> mapper.reportToCSVString(OperationSummaryReport.builder().build()))
            .isInstanceOf(UnprocessableException.class)
            .extracting("detailedReason")
            .isEqualTo("Operation summary report data is required.");
    }

    @Test
    void reportToCSVString_whenTotalsAreMissing_throwsUnprocessableException() {
        OperationSummaryReport report = OperationSummaryReport.builder()
            .summaryReport(SummaryReportDto.builder().build())
            .build();

        assertThatThrownBy(() -> mapper.reportToCSVString(report))
            .isInstanceOf(UnprocessableException.class)
            .extracting("detailedReason")
            .isEqualTo("Operation summary report totals are required.");
    }

    private static SummaryOperationReportRowDto summaryRow() {
        return SummaryOperationReportRowDto.builder()
            .header1("DETAIL")
            .company("N")
            .defendantName("Jones, Michael \"Mick\"")
            .accountNo("05000123E")
            .dateOfBirth(LocalDate.of(1970, 10, 10))
            .nationalInsuranceNo("AA123456A")
            .collectionOrder("Y")
            .lastMovementDate(LocalDate.of(2006, 2, 6))
            .amountImposed(new BigDecimal("250.00"))
            .amountPaid(new BigDecimal("65.00"))
            .balance(new BigDecimal("185.00"))
            .address1("85 High Street")
            .address2("Colchester")
            .address3("Essex")
            .postcode("CO8 6RR")
            .imposingCourt("Basildon Magistrates Court")
            .mobTel("07986-554423")
            .homeTel("01255-789333")
            .businessTel("01345-0123456")
            .vehicleReg("AC 51 GHF")
            .vehicleMake("Ford Focus")
            .email1("michaelianjones@example.com")
            .email2("mickey@example.com")
            .employeeRef("JONES25684E")
            .employerName("BigCo Ltd")
            .employerAddress1("BigCo House")
            .employerAddress2("55 High Street")
            .employerAddress3("Springfield")
            .employerAddress4("Chelmsford")
            .employerAddress5("Essex")
            .employerPostcode("CM7 6TT")
            .employerTel("01245-000000")
            .employerEmail("info@bigco.co.uk")
            .lastEnforcement("MAN")
            .lastEnforcementDate(LocalDate.of(2007, 2, 6))
            .enforcementReason("Personal circumstances")
            .earliestReleaseDate(LocalDate.of(2007, 2, 7))
            .user("NT")
            .enforcingCourtCode("100")
            .warrantRef("100/16/00003")
            .jailDays(10)
            .parentOrGuardian("N")
            .prosecutorCaseReference("01TRT884424L")
            .build();
    }
}
