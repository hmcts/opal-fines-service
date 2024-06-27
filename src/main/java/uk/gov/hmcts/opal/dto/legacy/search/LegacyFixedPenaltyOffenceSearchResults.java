package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyFixedPenaltyOffenceSearchResults {

    @XmlElement(name = "fixedPenaltyOffenceEntity")
    private List<FixedPenaltyOffenceEntity> fixedPenaltyOffenceEntities;
    private int totalCount;
}
