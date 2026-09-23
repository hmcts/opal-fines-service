package uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Transaction {
    private String transactionCode;
    private OriginatorDetails originatorDetails;
    private Long amount;
    private String dateEntryApplied;

    public boolean isCheque() {
        return transactionCode.equals("11");
    }

    public boolean isValidTransaction() {
        return Set.of("00", "15", "68", "93", "99").contains(transactionCode);
    }
}

