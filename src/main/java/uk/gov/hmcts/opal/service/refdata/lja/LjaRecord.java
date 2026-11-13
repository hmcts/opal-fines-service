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
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LjaRecord implements ToJsonString {

    public static final String COURT_ADDRESS_TYPE = "Court Address";

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
}
