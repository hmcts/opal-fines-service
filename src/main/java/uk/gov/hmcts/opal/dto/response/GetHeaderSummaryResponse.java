package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;

/**
 * Wrapper that flattens the DTO into the JSON body and
 * retains version only for ETag (not serialized).
 */
public class GetHeaderSummaryResponse implements GetResponse<DefendantAccountHeaderSummary> {

    @JsonUnwrapped
    private final DefendantAccountHeaderSummary data;

    @JsonIgnore // critical: do NOT put version in JSON; it goes to the ETag header
    private final Long version;

    public GetHeaderSummaryResponse(DefendantAccountHeaderSummary data, Long version) {
        this.data = data;
        this.version = version;
    }

    @Override
    public DefendantAccountHeaderSummary getData() {
        return data;
    }

    @Override
    public Long getVersion() {
        return version;
    }

}
