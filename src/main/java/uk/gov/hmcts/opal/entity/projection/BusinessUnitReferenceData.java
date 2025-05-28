package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.opal.entity.BusinessUnitRef;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BusinessUnitReferenceData(

    @JsonProperty("business_unit_id")
    Short businessUnitId,

    @JsonProperty("business_unit_name")
    String businessUnitName,

    @JsonProperty("business_unit_code")
    String businessUnitCode,

    @JsonProperty("business_unit_type")
    String businessUnitType,

    @JsonProperty("account_number_prefix")
    String accountNumberPrefix,

    @JsonProperty("opal_domain")
    String opalDomain,

    @JsonProperty("welsh_language")
    Boolean welshLanguage,

    List<ConfigItemRefData> configurationItems
) implements BusinessUnitRef {

    @Override
    public Short getBusinessUnitId() {
        return businessUnitId;
    }

    public record ConfigItemRefData(
        @JsonProperty("item_name") String itemName,
        @JsonProperty("item_value") String itemValue,
        @JsonProperty("item_values") List<String> itemValues) {}

}
