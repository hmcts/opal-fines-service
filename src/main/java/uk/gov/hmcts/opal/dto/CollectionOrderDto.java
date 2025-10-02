package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the "collection_order" object defined in collectionOrder.json schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionOrderDto implements ToJsonString {

    @JsonProperty("collection_order_flag")
    private Boolean collectionOrderFlag;

    @JsonProperty("collection_order_date")
    private String collectionOrderDate;
}
