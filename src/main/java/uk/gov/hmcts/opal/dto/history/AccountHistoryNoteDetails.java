package uk.gov.hmcts.opal.dto.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryNoteDetails implements AccountHistoryDetails {

    private String noteText;
}
