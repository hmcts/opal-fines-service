package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantTransactionDetails implements DefendantAccountHistoryDetails {

    private DefendantTransactionTypeReference transactionType;

    private PaymentMethodReference paymentMethod;

    private String paymentReference;

    private String additionalInformation;

    private WriteOffTypeReference writeOff;

    private DefendantTransactionStatusReference status;

    private LocalDateTime statusDate;

    private String associatedRecordType;

    private String associatedRecordId;

    private String accountNumber;

    private String sendingCourt;

    private LocalDate impositionDate;

    private String impositionCode;

    private BigDecimal amountImposed;
}
