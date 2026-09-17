package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorSummaryLegacy {

    @JsonProperty("creditor_account_type")
    @XmlElement(name = "creditor_account_type")
    private CreditorAccountTypeReference creditorAccountType;

    @JsonProperty("creditor_account_id")
    @XmlElement(name = "creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("major_creditor_name")
    @XmlElement(name = "major_creditor_name")
    private String majorCreditorName;

    @JsonProperty("minor_creditor_organisation_flag")
    @XmlElement(name = "minor_creditor_organisation_flag")
    private Boolean minorCreditorOrganisationFlag;

    @JsonProperty("individual_name")
    @XmlElement(name = "individual_name")
    private IndividualNameLegacy individualName;

    @JsonProperty("company_name")
    @XmlElement(name = "company_name")
    private CompanyNameLegacy companyName;
}
