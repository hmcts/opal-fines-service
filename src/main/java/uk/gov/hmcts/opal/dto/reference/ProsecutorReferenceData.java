package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProsecutorReferenceData(
    @JsonProperty("prosecutor_id") Short prosecutorId,
    @JsonProperty("name") String name,
    @JsonProperty("prosecutor_code") String prosecutorCode,
    @JsonProperty("address_line_1") String addressLine1,
    @JsonProperty("address_line_2") String addressLine2,
    @JsonProperty("address_line_3") String addressLine3,
    @JsonProperty("address_line_4") String addressLine4,
    @JsonProperty("address_line_5") String addressLine5,
    @JsonProperty("postcode") String postcode,
    @JsonProperty("end_date") @Temporal(TemporalType.TIMESTAMP) LocalDateTime endDate) {
}
