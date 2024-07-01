package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.entity.AliasEntity;

import java.util.List;

@XmlRootElement
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyAliasSearchResults {

    @XmlElement(name = "aliasEntity")
    private List<AliasEntity> aliasEntities;
    private Long totalCount;
}
