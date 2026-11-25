package uk.gov.hmcts.opal.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

public interface SuspenseTransactionHistoryProjection {

    Long getTransactionId();

    String getTransactionType();

    BigDecimal getAmount();

    LocalDateTime getPostedDate();

    @Nullable
    String getPostedBy();

    @Nullable
    String getPostedByName();

    @Nullable
    String getAssociatedRecordType();

    @Nullable
    String getAssociatedRecordId();

    @Nullable
    String getText();

    @Nullable
    String getReversed();

    String getSuspenseItemType();

    @Nullable
    Long getPaymentInId();

    @Nullable
    Boolean getAutoPayment();

    @Nullable
    String getPaymentMethod();

    @Nullable
    String getPaymentReceivedFrom();

    @Nullable
    String getPaymentReference();

    @Nullable
    String getOriginatorReference();

    @Nullable
    String getPaymentSource();

    @Nullable
    String getRelevantDetails();

    @Nullable
    String getCourtFeeCode();

    @Nullable
    String getPayerType();

    @Nullable
    String getPayerName();

    @Nullable
    String getPayerAddress();

    @Nullable
    String getDefendantAccountNumber();

    @Nullable
    String getDefendantOrganisationName();

    @Nullable
    String getDefendantTitle();

    @Nullable
    String getDefendantForenames();

    @Nullable
    String getDefendantSurname();

    @Nullable
    String getCreditorAccountNumber();

    @Nullable
    String getCreditorAccountType();

    @Nullable
    String getMajorCreditorName();

    @Nullable
    String getCreditorOrganisationName();

    @Nullable
    String getCreditorForenames();

    @Nullable
    String getCreditorSurname();

    @Nullable
    String getCreditorAddressLine1();

    @Nullable
    String getCreditorAddressLine2();

    @Nullable
    String getCreditorAddressLine3();

    @Nullable
    String getCreditorAddressLine4();

    @Nullable
    String getCreditorAddressLine5();

    @Nullable
    String getCreditorPostcode();

    @Nullable
    Long getDefendantPartyId();

    @Nullable
    Long getImpositionId();

    @Nullable
    Long getCreditorAccountId();

    @Nullable
    Long getCreditorPartyId();

    @Nullable
    Boolean getDefendantOrganisation();

    @Nullable
    Boolean getRepaymentCreditor();

    @Nullable
    Boolean getPayByBacs();

    @Nullable
    Boolean getCreditorOrganisation();

}
