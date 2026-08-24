package uk.gov.hmcts.opal.dto.history;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryPostedDetails {

    private LocalDateTime postedDate;

    private String postedBy;

    private String postedByName;
}
