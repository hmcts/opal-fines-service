package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportMetaData {
    private List<Integer> pdpoPartyIds;

}
