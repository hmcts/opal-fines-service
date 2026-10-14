package uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
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
public class InterfaceFileCommonDataExtract {

    private String fileName;
    private DestinationDetails destinationDetails;
    private PaymentType paymentType;
    private List<Transaction> transactions;
    private String dwpCourtCode;

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

}

