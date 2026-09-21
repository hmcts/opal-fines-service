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

    @JsonProperty("cja_code")
    private String cjaCode;

    @JsonProperty("common_platform_uuid")
    private String commonPlatformUUID;

    @JsonProperty("division_code")
    private String divisionCode;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("lja_code")
    private String ljaCode;

    @JsonProperty("lja_name")
    private String ljaName;

    @JsonProperty("lja_type")
    private String ljaType;

    @JsonProperty("lja_welsh_name")
    private String ljaWelshName;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("cluster_information_code")
    private Integer clusterInformationCode;

    @JsonProperty("enforcement_area_code")
    private Integer enforcementAreaCode;

    @JsonProperty("region_code")
    private Integer regionCode;

    @JsonProperty("addresses")
    private List<Address> addresses;

    @JsonProperty("contact_information")
    private List<ContactInformation> contactInformation;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactInformation implements ToJsonString {

        @JsonProperty("contact_sub_type")
        private String contactSubType;

        @JsonProperty("contact_type")
        private String contactType;

        @JsonProperty("contact_value")
        private String contactValue;
    }
}
