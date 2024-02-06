package uk.gov.hmcts.opal.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCourtSearch extends AddressSearch {
    private String businessUnitId;
    private String nameCy;
    private String addressLineCy;
}
