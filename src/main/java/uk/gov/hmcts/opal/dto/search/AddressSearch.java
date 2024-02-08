package uk.gov.hmcts.opal.dto.search;

import lombok.Data;

@Data
public abstract class AddressSearch {
    private String name;
    private String addressLine;
    private String postcode;
}
