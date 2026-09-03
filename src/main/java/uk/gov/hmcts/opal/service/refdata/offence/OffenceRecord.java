package uk.gov.hmcts.opal.service.refdata.offence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenceRecord implements ToJsonString {

    @JsonProperty("CJSCode")
    private String cjsCode;

    @JsonProperty("CJSTitle")
    private String cjsTitle;

    @JsonProperty("CJSTitleCY")
    private String cjsTitleCy;

    @JsonProperty("ActsAndSection")
    private String actsAndSection;

    @JsonProperty("ActAndSectionWelsh")
    private String actAndSectionWelsh;

    @JsonProperty("UsedFrom")
    private LocalDate usedFrom;

    @JsonProperty("UsedTo")
    private LocalDate usedTo;

    @JsonProperty("Recordable")
    private String recordable;

    @JsonProperty("Reportable")
    private String reportable;

    @JsonProperty("Custodianlindicator")
    private String custodianlIndicator;

    @JsonProperty("StandardList")
    private String standardList;

    @JsonProperty("TrafficControl")
    private Boolean trafficControl;

    @JsonProperty("DVLACode")
    private String dvlaCode;

    @JsonProperty("OffenceNotes")
    private String offenceNotes;

    @JsonProperty("MaximumPenalty")
    private Object maximumPenalty;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("HOClass")
    private String hoClass;

    @JsonProperty("HOSubClass")
    private String hoSubClass;

    @JsonProperty("ProceedingsCode")
    private String proceedingsCode;

    @JsonProperty("PNLDStandardOffenceWording")
    private String pnldStandardOffenceWording;

    @JsonProperty("PNLDWelshStandardOffenceWording")
    private String pnldWelshStandardOffenceWording;

    @JsonProperty("PNLDDateOfLastUpdate")
    private LocalDate pnldDateOfLastUpdate;

    @JsonProperty("PNLDProsecutionTimeLimit")
    private Object pnldProsecutionTimeLimit;

    @JsonProperty("PNLDMaxFineTypeMagistratesCourt")
    private Object pnldMaxFineTypeMagistratesCourt;

    @JsonProperty("PNLDMaxFineTypeMagistratesCourtDescription")
    private String pnldMaxFineTypeMagistratesCourtDescription;

    @JsonProperty("PNLDModeOfTrial")
    private String pnldModeOfTrial;

    @JsonProperty("PNLDEndorsableFlag")
    private String pnldEndorsableFlag;

    @JsonProperty("PNLDLocationFlag")
    private String pnldLocationFlag;

    @JsonProperty("PNLDPrincipalOffenceCategory")
    private String pnldPrincipalOffenceCategory;

    @JsonProperty("UserOffenceWording")
    private String userOffenceWording;

    @JsonProperty("UserStatementOfFacts")
    private String userStatementOfFacts;

    @JsonProperty("EntryPromptSubstitutionSOW")
    private String entryPromptSubstitutionSow;

    @JsonProperty("EntryPromptSubstitutionSOF")
    private String entryPromptSubstitutionSof;

    @JsonProperty("EntryPromptSubstitutionANS")
    private String entryPromptSubstitutionAns;

    @JsonProperty("CurrentEditor")
    private String currentEditor;

    @JsonProperty("Area")
    private String area;

    @JsonProperty("Blocked")
    private String blocked;

    @JsonProperty("SecondLanguageOffenceStatementOfFactsText")
    private String secondLanguageOffenceStatementOfFactsText;

    @JsonProperty("SecondLanguageOffenceWordingText")
    private String secondLanguageOffenceWordingText;

    @JsonProperty("OffenceCode")
    private String offenceCode;

    @JsonProperty("PNLDOffenceStartDate")
    private LocalDate pnldOffenceStartDate;

    @JsonProperty("PNLDOffenceEndDate")
    private LocalDate pnldOffenceEndDate;

    @JsonProperty("SOWReference")
    private String sowReference;

    @JsonProperty("PublishingStatus")
    private String publishingStatus;

    @JsonProperty("OffenceType")
    private String offenceType;

    @JsonProperty("OffenceSource")
    private String offenceSource;

    @JsonProperty("MISClassification")
    private String misClassification;

    @JsonProperty("OffenceClass")
    private String offenceClass;

    @JsonProperty("CanBeBulk")
    private Boolean canBeBulk;

    @JsonProperty("InitialFeeApplicable")
    private Boolean initialFeeApplicable;

    @JsonProperty("ContestedFee")
    private Boolean contestedFee;

    @JsonProperty("ApplicationSynonym")
    private String applicationSynonym;

    @JsonProperty("Exparte")
    private Boolean exparte;

    @JsonProperty("Jurisdiction")
    private String jurisdiction;

    @JsonProperty("AppealFlag")
    private Boolean appealFlag;

    @JsonProperty("SummonsTemplateType")
    private String summonsTemplateType;

    @JsonProperty("LinkType")
    private String linkType;

    @JsonProperty("HearingCode")
    private String hearingCode;

    @JsonProperty("ApplicantAppellantFlag")
    private String applicantAppellantFlag;

    @JsonProperty("PleaApplicableFlag")
    private Boolean pleaApplicableFlag;

    @JsonProperty("ActiveOffenceOrder")
    private Integer activeOffenceOrder;

    @JsonProperty("CommissionerOfOath")
    private Boolean commissionerOfOath;

    @JsonProperty("BreachType")
    private String breachType;

    @JsonProperty("CourtOfAppealFlag")
    private Boolean courtOfAppealFlag;

    @JsonProperty("CourtExtractAvailable")
    private Boolean courtExtractAvailable;

    @JsonProperty("ListingNotificationTemplate")
    private String listingNotificationTemplate;

    @JsonProperty("BoxworkNotificationTemplate")
    private String boxworkNotificationTemplate;

    @JsonProperty("ProsecutorAsThirdPartyFlag")
    private Boolean prosecutorAsThirdPartyFlag;

    @JsonProperty("ResentencingActivationCode")
    private String resentencingActivationCode;

    @JsonProperty("Prefix")
    private String prefix;

    @JsonProperty("ObsoleteIndicator")
    private String obsoleteIndicator;
}
