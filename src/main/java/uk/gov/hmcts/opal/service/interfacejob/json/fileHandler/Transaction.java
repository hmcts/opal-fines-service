package uk.gov.hmcts.opal.service.interfacejob.json.fileHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    private String transactionCode;
    private OriginatorDetails originatorDetails;
    private Long amount;
    private String dateEntryApplied;
}

