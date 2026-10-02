package uk.gov.hmcts.opal.service.interfacejob.json.records;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterfaceJobRecord {
    private String destinationSortCode;
    private String destinationBankAccountNumber;
    private String destinationAccountType;
    private String destinationBeneficiaryName;
    private String transactionCode;
    private String originatorSortCode;
    private String originatorBankAccountNumber;
    private String originatorName;
    private String originatorReference;
    private int amountPence;
}
