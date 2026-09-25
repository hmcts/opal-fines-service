package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetDefendantAccountImpositionsLegacyResponse {

    @XmlElement(name = "version")
    private BigInteger version;

    @XmlElementWrapper(name = "impositions")
    @XmlElement(name = "impositions_element")
    private List<Imposition> impositions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Imposition {

        @XmlElement(name = "posted_details")
        private PostedDetails postedDetails;

        @XmlElement(name = "result")
        private Result result;

        @XmlElement(name = "creditor")
        private Creditor creditor;

        @XmlElement(name = "imposed_amount")
        private BigDecimal imposedAmount;

        @XmlElement(name = "paid_amount")
        private BigDecimal paidAmount;

        @XmlElement(name = "balance")
        private BigDecimal balance;

        @XmlElement(name = "date_imposed")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate dateImposed;

        @XmlElement(name = "offence")
        private Offence offence;

        @XmlElement(name = "imposition_id")
        private Long impositionId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PostedDetails {

        @XmlElement(name = "posted_date")
        @XmlJavaTypeAdapter(LegacyDateTimeAdapter.class)
        private LocalDateTime postedDate;

        @XmlElement(name = "posted_by")
        private String postedBy;

        @XmlElement(name = "posted_by_name")
        private String postedByName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Result {

        @XmlElement(name = "result_id")
        private String resultId;

        @XmlElement(name = "result_title")
        private String resultTitle;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Creditor {

        @XmlElement(name = "creditor_account_type")
        private CreditorAccountType creditorAccountType;

        @XmlElement(name = "creditor_account_id")
        private Long creditorAccountId;

        @XmlElement(name = "major_creditor_name")
        private String majorCreditorName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreditorAccountType {

        @XmlElement(name = "creditor_account_type")
        private String creditorAccountType;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Offence {

        @XmlElement(name = "offence_id")
        private Long offenceId;

        @XmlElement(name = "cjs_code")
        private String cjsCode;

        @XmlElement(name = "offence_title")
        private String offenceTitle;
    }

    public static class LegacyDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(String value) {
            return LocalDateTime.parse(value.replace(' ', 'T'));
        }

        @Override
        public String marshal(LocalDateTime value) {
            return value.toString().replace('T', ' ');
        }
    }
}
