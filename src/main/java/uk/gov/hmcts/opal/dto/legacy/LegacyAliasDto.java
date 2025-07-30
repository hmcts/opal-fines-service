package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.search.AliasDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "aliases_element")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyAliasDto {

    @XmlElement(name = "alias_number")
    private int aliasNumber;
    // Organisation-specific
    @XmlElement(name = "organisation_name")
    private String organisationName;
    // Individual-specific
    @XmlElement(name = "surname")
    private String surname;
    @XmlElement(name = "forenames")
    private String forenames;

    public AliasDto toAliasDto() {
        return AliasDto.builder()
            .aliasNumber(aliasNumber)
            .organisationName(organisationName)
            .surname(surname)
            .forenames(forenames)
            .build();
    }

}
