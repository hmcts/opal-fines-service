package uk.gov.hmcts.opal.service.refdata.prison;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class PrisonRecord implements ToJsonString {

    private static final String PRISON_ADDRESS_TYPE = "PRIS";

    private String name;

    @JsonProperty("welsh_name")
    private String welshName;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("publishing_status")
    private String publishingStatus;

    private String notes;
    private List<Address> addresses;

    public Optional<Address> prisonAddress() {
        if (addresses == null) {
            return Optional.empty();
        }
        return addresses.stream()
            .filter(Objects::nonNull)
            .filter(address -> PRISON_ADDRESS_TYPE.equals(address.getAddressType()))
            .findFirst();
    }
}
