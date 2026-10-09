package uk.gov.hmcts.opal.service.interfacejob.json.filehandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BankDetails {
    private String accountNumber;
    private String sortCode;
    private String name;
    private String type;
}

