package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;

import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyMisDebtorSearchResults {

    @XmlElement(name = "misDebtorEntity")
    private List<MisDebtorEntity> misDebtorEntities;
    private int totalCount;
}
