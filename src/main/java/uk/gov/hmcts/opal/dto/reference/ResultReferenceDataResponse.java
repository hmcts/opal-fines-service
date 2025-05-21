package uk.gov.hmcts.opal.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResultReferenceDataResponse {
    private Integer count;
    private List<ResultReferenceData> refData;

    public static class ResultReferenceDataResponseBuilder {

        public ResultReferenceDataResponse.ResultReferenceDataResponseBuilder refData(
            List<ResultReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private ResultReferenceDataResponse.ResultReferenceDataResponseBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
