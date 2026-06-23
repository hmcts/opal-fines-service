package uk.gov.hmcts.opal.service.report.mapper.csv;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementDetailedReport;

/**
 * Generate a CSV String for Operation By Enforcement and by payment Detailed Reports.
 * CSV lines order:
 * - Header 1
 * - Header 2
 * - account (based on header 1)
 * -- each transaction for the account (based on)
 * - next account (etc.)
 */
@Component
public class OperationByEnforcementReportDetailedCSVMapper
    implements ReportCSVMapper<OperationByEnforcementDetailedReport> {
    private List<String> header1Row() {
        return new java.util.ArrayList<>(List.of(
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
        ));
    }

    private List<String> header2Row() {
        return List.of(
            "HEADER2",
            "accountno",
            "conacno",
            "txndate",
            "txntype",
            "txndets",
            "txnuser",
            "txnamount"
        );
    }

    private List<String> accountRow(OperationByEnforcementDetailedReportAccountRowDto accountRow) {
        return List.of(
            getDataValue(accountRow.getHeader1()),
            getDataValue(accountRow.getCompany()),
            getDataValue(accountRow.getAccountNo()),
            getDataValue(accountRow.getDefendantName()),
            getDataValue(accountRow.getDateOfBirth()),
            getDataValue(accountRow.getAddress1()),
            getDataValue(accountRow.getAddress2()),
            getDataValue(accountRow.getAddress3()),
            getDataValue(accountRow.getPostcode()),
            getDataValue(accountRow.getEmployeeRef()),
            getDataValue(accountRow.getEmployerName()),
            getDataValue(accountRow.getEmployerAddress1()),
            getDataValue(accountRow.getEmployerAddress2()),
            getDataValue(accountRow.getEmployerAddress3()),
            getDataValue(accountRow.getEmployerAddress4()),
            getDataValue(accountRow.getEmployerAddress5()),
            getDataValue(accountRow.getEmployerPostcode()),
            getDataValue(accountRow.getEmployerTel()),
            getDataValue(accountRow.getEmployerEmail()),
            getDataValue(accountRow.getCollectionOrder()),
            getDataValue(accountRow.getLastMovementDate()),
            getDataValue(accountRow.getDateOfHearing()),
            getDataValue(accountRow.getImposingCourt()),
            getDataValue(accountRow.getPaymentTerms()),
            getDataValue(accountRow.getAmountImposed()),
            getDataValue(accountRow.getBalance()),
            getDataValue(accountRow.getArrearsTotal()),
            getDataValue(accountRow.getFineImpositions()),
            getDataValue(accountRow.getCostImpositions()),
            getDataValue(accountRow.getCompensationImpositions()),
            getDataValue(accountRow.getCriminalCourtsChargeImpositions()),
            getDataValue(accountRow.getVictimSurchargeImpositions()),
            getDataValue(accountRow.getOtherImpositions()),
            getDataValue(accountRow.getProsecutorCaseReference())
        );
    }

    private List<String> transactionRow(OperationByEnforcementDetailedReportTransactionRowDto transactionRow) {
        return List.of(
            "TRANSACTION",
            getDataValue(transactionRow.getAccountNo()),
            getDataValue(transactionRow.getConsolidatedAccountNo()),
            getDataValue(transactionRow.getTransactionDate()),
            getDataValue(transactionRow.getTransactionType()),
            EMPTY_VALUE,
            getDataValue(transactionRow.getTransactionUserId()),
            getDataValue(transactionRow.getTransactionAmount())
        );
    }


    @Override
    public String reportToCSVString(OperationByEnforcementDetailedReport operationByEnforcementDetailedReport) {
        StringBuilder sb = new StringBuilder();
        sb.append(dataListToFullCSVRow(header1Row())).append(dataListToFullCSVRow(header2Row()));
        for (OperationByEnforcementDetailedAccountReportDto dto :
            operationByEnforcementDetailedReport.getEnforcementReport().getAccountTransactionReports()) {
            sb.append(dataListToFullCSVRow(accountRow(dto.getAccountRow())));
            for (OperationByEnforcementDetailedReportTransactionRowDto transactionRow : dto.getTransactionRows()) {
                sb.append(dataListToFullCSVRow(transactionRow(transactionRow)));
            }
        }
        return sb.toString();
    }
}
