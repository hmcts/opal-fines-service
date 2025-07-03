package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefDataResponse<T> implements ToJsonString {
    private Integer count;
    @JsonProperty("ref_data")
    private List<T> refData;

    public static class RefDataResponseBuilder<T> {
        public RefDataResponseBuilder<T> refData(List<T> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private RefDataResponseBuilder<T> count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
