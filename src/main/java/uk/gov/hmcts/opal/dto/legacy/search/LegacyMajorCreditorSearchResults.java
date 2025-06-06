package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyMajorCreditorSearchResults {

    @XmlElement(name = "majorCreditorEntity")
    private List<MajorCreditorEntity> majorCreditorEntities;
    private int totalCount;
}
