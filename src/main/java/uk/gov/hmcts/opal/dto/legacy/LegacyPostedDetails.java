package uk.gov.hmcts.opal.dto.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyPostedDetails {

    private LocalDate postedDate;

    private String postedBy;

    private String postedByName;
}
