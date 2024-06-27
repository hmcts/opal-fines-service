package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyLocalJusticeAreaSearchResults {

    @XmlElement(name = "localJusticeAreaEntity")
    private List<LocalJusticeAreaEntity> localJusticeAreaEntities;
    private int totalCount;
}
