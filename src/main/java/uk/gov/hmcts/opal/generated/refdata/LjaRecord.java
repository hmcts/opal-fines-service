package uk.gov.hmcts.opal.generated.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class LjaRecord {

    @JsonProperty("LJACode")
    private String ljaCode;

    @JsonProperty("LJAName")
    private String name;

    @JsonProperty("EndDate")
    private LocalDate endDate;

    @JsonProperty("Addresses")
    private List<Address> addresses = new ArrayList<>();
}
