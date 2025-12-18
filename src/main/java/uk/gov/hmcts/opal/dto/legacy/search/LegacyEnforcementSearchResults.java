package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyEnforcementSearchResults {

    @XmlElement(name = "enforcementEntity")
    private List<EnforcementEntity.Lite> enforcementEntities;
    private int totalCount;
}
