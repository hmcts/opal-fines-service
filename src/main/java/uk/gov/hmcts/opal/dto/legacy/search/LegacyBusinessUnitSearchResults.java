package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyBusinessUnitSearchResults {

    @XmlElement(name = "businessUnitEntity")
    private List<BusinessUnit.Lite> businessUnitEntities;
    private int totalCount;
}
