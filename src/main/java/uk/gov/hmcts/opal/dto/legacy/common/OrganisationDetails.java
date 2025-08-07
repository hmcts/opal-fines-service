package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisationDetails {

    @XmlElement(name = "organisation_name")
    private String organisationName;

    @XmlElement(name = "organisation_aliases")
    private OrganisationAlias[] organisationAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OrganisationAlias {

        @XmlElement(name = "alias_id")
        private String aliasId;

        @XmlElement(name = "sequence_number")
        private Short sequenceNumber;

        @XmlElement(name = "organisation_name")
        private String organisationName;

    }
}
