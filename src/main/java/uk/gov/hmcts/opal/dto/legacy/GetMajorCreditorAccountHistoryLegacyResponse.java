package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMajorCreditorAccountHistoryLegacyResponse implements ToXmlString {

    @XmlElement(name = "version")
    private Long version;

    @JsonProperty("history_items")
    @XmlElementWrapper(name = "history_items")
    @XmlElement(name = "history_items_element")
    private List<LegacyMajorCreditorHistoryItem> historyItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyMajorCreditorHistoryItem {

        @JsonProperty("posted_details")
        @XmlElement(name = "posted_details")
        private LegacyPostedDetails postedDetails;

        @JsonProperty("type")
        @XmlElement(name = "type")
        private String type;

        @JsonProperty("details")
        @XmlElement(name = "details")
        private LegacyMajorCreditorHistoryDetails details;

        @JsonProperty("amount")
        @XmlElement(name = "amount")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyMajorCreditorHistoryDetails {

        @JsonProperty("transaction_type")
        @XmlElement(name = "transaction_type")
        private LegacyCreditorTransactionTypeReference transactionType;

        @JsonProperty("payment_reference")
        @XmlElement(name = "payment_reference")
        private String paymentReference;

        @JsonProperty("status")
        @XmlElement(name = "status")
        private LegacyCreditorTransactionStatusReference status;

        @JsonProperty("status_date")
        @XmlElement(name = "status_date")
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime statusDate;

        @JsonProperty("associated_record_type")
        @XmlElement(name = "associated_record_type")
        private String associatedRecordType;

        @JsonProperty("associated_record_id")
        @XmlElement(name = "associated_record_id")
        private String associatedRecordId;

        @JsonProperty("account_number")
        @XmlElement(name = "account_number")
        private String accountNumber;

        @JsonProperty("defendant_account_number")
        @XmlElement(name = "defendant_account_number")
        private String defendantAccountNumber;

        @JsonProperty("defendant_account_id")
        @XmlElement(name = "defendant_account_id")
        private Long defendantAccountId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyCreditorTransactionTypeReference {

        @JsonProperty("transaction_type")
        @XmlElement(name = "transaction_type")
        private String transactionType;

        @JsonProperty("transaction_type_display_name")
        @XmlElement(name = "transaction_type_display_name")
        private String transactionTypeDisplayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyCreditorTransactionStatusReference {

        @JsonProperty("creditor_transaction_status")
        @XmlElement(name = "creditor_transaction_status")
        private String creditorTransactionStatus;

        @JsonProperty("creditor_transaction_status_display_name")
        @XmlElement(name = "creditor_transaction_status_display_name")
        private String creditorTransactionStatusDisplayName;
    }
}
