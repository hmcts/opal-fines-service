package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedPenaltyOffenceSearchDto implements ToJsonString {

    private String defendantAccountId;
    private String ticketNumber;
    private String vehicleRegistration;
    private String offenceLocation;
    private String noticeNumber;
    private String licenceNumber;

}
