package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.util.LocalDateAdapter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetDefendantAccountHistoryLegacyResponse implements ToXmlString {

    @XmlElement(name = "version")
    private Long version;

    @JsonProperty("history_items")
    @XmlElementWrapper(name = "historyItems")
    @XmlElement(name = "historyItems_element")
        private List<LegacyDefendantAccountHistoryItem> historyItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyDefendantAccountHistoryItem {

        @JsonProperty("posted_details")
        @XmlElement(name = "postedDetails")
        private LegacyPostedDetails postedDetails;

        @JsonProperty("type")
        @XmlElement(name = "type")
        private String type;

        @JsonProperty("details")
        @XmlElement(name = "details")
        private LegacyDefendantAccountHistoryDetails details;

        @JsonProperty("amount")
        @XmlElement(name = "amount")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyDefendantAccountHistoryDetails {

        @JsonProperty("attribute_name")
        @XmlElement(name = "attributeName")
        private String attributeName;

        @JsonProperty("old_value")
        @XmlElement(name = "oldValue")
        private String oldValue;

        @JsonProperty("new_value")
        @XmlElement(name = "newValue")
        private String newValue;

        @JsonProperty("enforcement_action")
        @XmlElement(name = "enforcementAction")
        private String enforcementAction;

        @JsonProperty("days_in_default")
        @XmlElement(name = "daysInDefault")
        private Integer daysInDefault;

        @JsonProperty("warrant_number")
        @XmlElement(name = "warrantNumber")
        private String warrantNumber;

        @JsonProperty("hearing_date")
        @XmlElement(name = "hearing_date")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate hearingDate;

        @JsonProperty("hearing_court")
        @XmlElement(name = "hearing_court")
        private CourtReference hearingCourt;

        @JsonProperty("case_number")
        @XmlElement(name = "caseNumber")
        private String caseNumber;

        @JsonProperty("reason")
        @XmlElement(name = "reason")
        private String reason;

        @JsonProperty("earliest_date_of_release")
        @XmlElement(name = "earliest_date_of_release")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate earliestDateOfRelease;

        @JsonProperty("note_text")
        @XmlElement(name = "noteText")
        private String noteText;

        @JsonProperty("transaction_type")
        @XmlElement(name = "transactionType")
        private LegacyHistoryTypeReference transactionType;

        @JsonProperty("payment_method")
        @XmlElement(name = "paymentMethod")
        private LegacyHistoryTypeReference paymentMethod;

        @JsonProperty("payment_reference")
        @XmlElement(name = "paymentReference")
        private String paymentReference;

        @JsonProperty("additional_information")
        @XmlElement(name = "additionalInformation")
        private String additionalInformation;

        @JsonProperty("write_off")
        @XmlElement(name = "write_off")
        private LegacyHistoryTypeReference writeOff;

        @JsonProperty("status")
        @XmlElement(name = "status")
        private LegacyHistoryTypeReference status;

        @JsonProperty("status_date")
        @XmlElement(name = "statusDate")
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime statusDate;

        @JsonProperty("associated_record_type")
        @XmlElement(name = "associatedRecordType")
        private String associatedRecordType;

        @JsonProperty("associated_record_id")
        @XmlElement(name = "associatedRecordId")
        private String associatedRecordId;

        @JsonProperty("account_number")
        @XmlElement(name = "accountNumber")
        private String accountNumber;

        @JsonProperty("sending_court")
        @XmlElement(name = "sendingCourt")
        private String sendingCourt;

        @JsonProperty("imposition_date")
        @XmlElement(name = "imposition_date")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate impositionDate;

        @JsonProperty("imposition_code")
        @XmlElement(name = "imposition_code")
        private String impositionCode;

        @JsonProperty("amount_imposed")
        @XmlElement(name = "amount_imposed")
        private BigDecimal amountImposed;

        @JsonProperty("date_days_in_default_imposed")
        @XmlElement(name = "date_days_in_default_imposed")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate dateDaysInDefaultImposed;

        @JsonProperty("reason_for_extension")
        @XmlElement(name = "reason_for_extension")
        private String reasonForExtension;

        @JsonProperty("payment_terms_type")
        @XmlElement(name = "payment_terms_type")
        private LegacyPaymentTermsType paymentTermsType;

        @JsonProperty("effective_date")
        @XmlElement(name = "effective_date")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate effectiveDate;

        @JsonProperty("instalment_period")
        @XmlElement(name = "instalment_period")
        private LegacyInstalmentPeriod instalmentPeriod;

        @JsonProperty("lump_sum_amount")
        @XmlElement(name = "lump_sum_amount")
        private BigDecimal lumpSumAmount;

        @JsonProperty("instalment_amount")
        @XmlElement(name = "instalment_amount")
        private BigDecimal instalmentAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyHistoryTypeReference {

        @JsonProperty("transaction_type")
        @XmlElement(name = "transactionType")
        private String transactionType;

        @JsonProperty("transaction_type_display_name")
        @XmlElement(name = "transactionTypeDisplayName")
        private String transactionTypeDisplayName;

        @JsonProperty("payment_method")
        @XmlElement(name = "paymentMethod")
        private String paymentMethod;

        @JsonProperty("payment_method_display_name")
        @XmlElement(name = "paymentMethodDisplayName")
        private String paymentMethodDisplayName;

        @JsonProperty("write_off_type")
        @XmlElement(name = "write_off_type")
        private String writeOffType;

        @JsonProperty("write_off_type_display_name")
        @XmlElement(name = "write_off_type_display_name")
        private String writeOffTypeDisplayName;

        @JsonProperty("defendant_transaction_status")
        @XmlElement(name = "defendantTransactionStatus")
        private String defendantTransactionStatus;

        @JsonProperty("defendant_transaction_status_display_name")
        @XmlElement(name = "defendantTransactionStatusDisplayName")
        private String defendantTransactionStatusDisplayName;
    }
}
