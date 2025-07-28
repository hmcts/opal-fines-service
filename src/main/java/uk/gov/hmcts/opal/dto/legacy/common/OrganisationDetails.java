package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("organisation_aliases")
    private OrganisationAlias[] organisationAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OrganisationAlias {

        @JsonProperty("alias_id")
        private String aliasId;

        @JsonProperty("sequence_number")
        private Short sequenceNumber;

        @JsonProperty("organisation_name")
        private String organisationName;

    }
}
