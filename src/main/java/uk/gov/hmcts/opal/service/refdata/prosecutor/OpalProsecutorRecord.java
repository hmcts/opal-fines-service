package uk.gov.hmcts.opal.service.refdata.prosecutor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpalProsecutorRecord implements ToJsonString {

    public static final String PROSECUTOR_ADDRESS_TYPE = "Prosecutor";

    @JsonProperty("prosecutor_code")
    private Integer prosecutorCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("welsh_name")
    private String welshName;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("publishing_status")
    private String publishingStatus;

    @JsonProperty("addresses")
    private List<Address> addresses;
}
