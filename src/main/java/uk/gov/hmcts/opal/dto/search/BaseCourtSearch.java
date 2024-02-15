package uk.gov.hmcts.opal.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class BaseCourtSearch extends AddressSearch {
    private String businessUnitId;
    private String nameCy;
    private String addressLineCy;
}
