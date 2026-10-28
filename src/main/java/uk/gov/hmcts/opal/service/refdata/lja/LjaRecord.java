package uk.gov.hmcts.opal.service.refdata.lja;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LjaRecord implements ToJsonString {

    @JsonProperty("lja_code")
    private String ljaCode;

    @JsonProperty("lja_name")
    private String ljaName;

    @JsonProperty("lja_type")
    private String ljaType;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("addresses")
    private List<Address> addresses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address implements ToJsonString {

        @JsonProperty("address_line_1")
        private String addressLine1;

        @JsonProperty("address_line_2")
        private String addressLine2;

        @JsonProperty("address_line_3")
        private String addressLine3;

        @JsonProperty("address_line_4")
        private String addressLine4;

        @JsonProperty("address_line_5")
        private String addressLine5;

        @JsonProperty("address_type")
        private String addressType;

        @JsonProperty("post_code")
        private String postCode;
    }
}
