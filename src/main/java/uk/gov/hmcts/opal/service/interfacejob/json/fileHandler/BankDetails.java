package uk.gov.hmcts.opal.service.interfacejob.json.fileHandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class BankDetails {
    @NonNull
    private String accountNumber;
    @NonNull
    private String sortCode;
    @NonNull
    private String name;
    @NonNull
    private String type;
}

