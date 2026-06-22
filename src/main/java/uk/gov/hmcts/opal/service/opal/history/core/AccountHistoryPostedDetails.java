package uk.gov.hmcts.opal.service.opal.history.core;

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
