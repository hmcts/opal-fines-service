package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyOrganisationDetails {

    @XmlElement(name = "organisation_name")
    private String organisationName;

    @XmlElement(name = "organisation_aliases")
    private LegacyOrganisationAlias[] organisationAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyOrganisationAlias {

        @XmlElement(name = "alias_id")
        private String aliasId;

        @XmlElement(name = "sequence_number")
        private Short sequenceNumber;

        @XmlElement(name = "organisation_name")
        private String organisationName;

        public OrganisationAlias toOpalDto () {
            return OrganisationAlias.builder()
                .aliasId(this.getAliasId())
                .sequenceNumber(Integer.valueOf(this.getSequenceNumber()))
                .organisationName(this.getOrganisationName())
                .build();
        }

    }

    public OrganisationDetails toOpalDto () {
        return OrganisationDetails.builder()
            .organisationName(this.getOrganisationName())
            .organisationAliases(this.getOrganisationAliases() == null
                ? Collections.emptyList()
                : Arrays.stream(this.getOrganisationAliases())
                    .map(LegacyOrganisationAlias::toOpalDto)
                    .toList())
            .build();
    }
}
