package uk.gov.hmcts.opal.service.opal.history.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionStatusReference;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionTypeReference;
import uk.gov.hmcts.opal.dto.history.PaymentMethodReference;
import uk.gov.hmcts.opal.dto.history.WriteOffTypeReference;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryDefendantTransactionDetails implements AccountHistoryDetails {

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
