package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BusinessUnitReferenceData {

    @JsonProperty("business_unit_id")
    Short businessUnitId;

    @JsonProperty("business_unit_name")
    String businessUnitName;

    @JsonProperty("business_unit_code")
    String businessUnitCode;

    @JsonProperty("business_unit_type")
    String businessUnitType;

    @JsonProperty("account_number_prefix")
    String accountNumberPrefix;

    @JsonProperty("opal_domain")
    String opalDomain;

    @JsonProperty("welsh_language")
    Boolean welshLanguage;

    @JsonProperty("configuration_items")
    List<ConfigItemRefData> configurationItems;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ConfigItemRefData {

        @JsonProperty("item_name")
        String itemName;
        @JsonProperty("item_value")
        String itemValue;
        @JsonProperty("item_values")
        List<String> itemValues;
    }
}