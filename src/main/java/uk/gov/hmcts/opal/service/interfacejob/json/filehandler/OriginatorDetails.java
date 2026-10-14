package uk.gov.hmcts.opal.service.interfacejob.json.filehandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OriginatorDetails {
    private String name;
    private String accountReference;
    private BankDetails bankDetails;
}

