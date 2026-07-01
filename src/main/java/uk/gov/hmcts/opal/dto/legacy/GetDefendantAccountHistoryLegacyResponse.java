package uk.gov.hmcts.opal.dto.legacy;

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

    @XmlElementWrapper(name = "historyItems")
    @XmlElement(name = "historyItems_element")
    private List<LegacyDefendantAccountHistoryItem> historyItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyDefendantAccountHistoryItem {

        @XmlElement(name = "postedDetails")
        private LegacyPostedDetails postedDetails;

        @XmlElement(name = "type")
        private String type;

        @XmlElement(name = "details")
        private LegacyDefendantAccountHistoryDetails details;

        @XmlElement(name = "amount")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyDefendantAccountHistoryDetails {

        @XmlElement(name = "attributeName")
        private String attributeName;

        @XmlElement(name = "oldValue")
        private String oldValue;

        @XmlElement(name = "newValue")
        private String newValue;

        @XmlElement(name = "enforcementAction")
        private String enforcementAction;

        @XmlElement(name = "daysInDefault")
        private Integer daysInDefault;

        @XmlElement(name = "warrantNumber")
        private String warrantNumber;

        @XmlElement(name = "hearingDate")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate hearingDate;

        @XmlElement(name = "hearingCourt")
        private CourtReference hearingCourt;

        @XmlElement(name = "caseNumber")
        private String caseNumber;

        @XmlElement(name = "reason")
        private String reason;

        @XmlElement(name = "earliestDateOfRelease")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate earliestDateOfRelease;

        @XmlElement(name = "noteText")
        private String noteText;

        @XmlElement(name = "transactionType")
        private LegacyHistoryTypeReference transactionType;

        @XmlElement(name = "paymentMethod")
        private LegacyHistoryTypeReference paymentMethod;

        @XmlElement(name = "paymentReference")
        private String paymentReference;

        @XmlElement(name = "additionalInformation")
        private String additionalInformation;

        @XmlElement(name = "writeOff")
        private LegacyHistoryTypeReference writeOff;

        @XmlElement(name = "status")
        private LegacyHistoryTypeReference status;

        @XmlElement(name = "statusDate")
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime statusDate;

        @XmlElement(name = "associatedRecordType")
        private String associatedRecordType;

        @XmlElement(name = "associatedRecordId")
        private String associatedRecordId;

        @XmlElement(name = "accountNumber")
        private String accountNumber;

        @XmlElement(name = "sendingCourt")
        private String sendingCourt;

        @XmlElement(name = "impositionDate")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate impositionDate;

        @XmlElement(name = "impositionCode")
        private String impositionCode;

        @XmlElement(name = "amountImposed")
        private BigDecimal amountImposed;

        @XmlElement(name = "date_days_in_default_imposed")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate dateDaysInDefaultImposed;

        @XmlElement(name = "reason_for_extension")
        private String reasonForExtension;

        @XmlElement(name = "payment_terms_type")
        private LegacyPaymentTermsType paymentTermsType;

        @XmlElement(name = "effective_date")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate effectiveDate;

        @XmlElement(name = "instalment_period")
        private LegacyInstalmentPeriod instalmentPeriod;

        @XmlElement(name = "lump_sum_amount")
        private BigDecimal lumpSumAmount;

        @XmlElement(name = "instalment_amount")
        private BigDecimal instalmentAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyHistoryTypeReference {

        @XmlElement(name = "transactionType")
        private String transactionType;

        @XmlElement(name = "transactionTypeDisplayName")
        private String transactionTypeDisplayName;

        @XmlElement(name = "paymentMethod")
        private String paymentMethod;

        @XmlElement(name = "paymentMethodDisplayName")
        private String paymentMethodDisplayName;

        @XmlElement(name = "writeOffType")
        private String writeOffType;

        @XmlElement(name = "writeOffTypeDisplayName")
        private String writeOffTypeDisplayName;

        @XmlElement(name = "defendantTransactionStatus")
        private String defendantTransactionStatus;

        @XmlElement(name = "defendantTransactionStatusDisplayName")
        private String defendantTransactionStatusDisplayName;
    }
}
