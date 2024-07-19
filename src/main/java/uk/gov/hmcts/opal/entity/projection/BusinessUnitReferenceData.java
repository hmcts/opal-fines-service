package uk.gov.hmcts.opal.entity.projection;

import uk.gov.hmcts.opal.entity.BusinessUnitRef;

import java.util.List;

public record BusinessUnitReferenceData(Short businessUnitId,

    String businessUnitName,

    String businessUnitCode,

    String businessUnitType,

    String accountNumberPrefix,

    String opalDomain,

    Boolean welshLanguage,

    List<ConfigItemRefData> configurationItems
) implements BusinessUnitRef {

    @Override
    public Short getBusinessUnitId() {
        return businessUnitId;
    }

    public record ConfigItemRefData(String itemName, String itemValue, List<String> itemValues) {}

}
