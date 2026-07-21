package uk.gov.hmcts.opal.service.iface;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;

public interface MajorCreditorAccountServiceInterface {

    GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long majorCreditorAccountId);

    GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId);

    GetMajorCreditorHistoryResponse getHistory(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    );
}
