package uk.gov.hmcts.opal.dto.legacy;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountEnforcementStatusResponse implements ToXmlString {

    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "enforcement_overview")
    private EnforcementOverview enforcementOverview;

    @XmlElement(name = "enforcement_override")
    private EnforcementOverride enforcementOverride;

    @XmlElement(name = "last_enforcement_action")
    private EnforcementAction lastEnforcementAction;

    @XmlElement(name = "account_status_reference")
    private AccountStatusReference accountStatusReference;

    @XmlElement(name = "employer_flag")
    private String employerFlag;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class EnforcementOverview implements ToXmlString {

        @XmlElement(name = "days_in_default")
        private Integer daysInDefault;

        @XmlElement(name = "enforcement_court")
        private CourtReference enforcementCourt;

        @XmlElement(name = "collection_order")
        private CollectionOrder collectionOrder;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class EnforcementAction implements ToXmlString {

        @XmlElement(name = "result_reference")
        private ResultReference resultReference;

        @XmlElement(name = "reason")
        private String reason;

        @XmlElement(name = "enforcer")
        private EnforcerReference enforcer;

        @XmlElement(name = "warrant_number")
        private String warrantNumber;

        @XmlElement(name = "date_added")
        private String dateAdded;

        @XmlElement(name = "result_responses")
        private ResultResponses resultResponses;

    }
}
