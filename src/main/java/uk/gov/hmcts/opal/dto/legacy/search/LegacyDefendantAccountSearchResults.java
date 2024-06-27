package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyDefendantAccountSearchResults {

    @XmlElement(name = "defendantAccountEntity")
    private List<DefendantAccountEntity> defendantAccountEntities;
    private int totalCount;
}
