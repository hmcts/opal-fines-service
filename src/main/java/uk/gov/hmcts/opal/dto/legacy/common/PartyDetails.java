package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "party_details", propOrder = {
    "partyId",
    "organisationFlag",
    "organisationDetails",
    "individualDetails"
})
public class PartyDetails {

    @XmlElement(name = "party_id", required = true)
    @NotNull
    @Size(min = 1)
    private String partyId;

    @XmlElement(name = "organisation_flag", required = true)
    @NotNull
    private Boolean organisationFlag;

    @XmlElement(name = "organisation_details")
    private OrganisationDetails organisationDetails;


    @XmlElement(name = "individual_details")
    private IndividualDetails individualDetails;

}

