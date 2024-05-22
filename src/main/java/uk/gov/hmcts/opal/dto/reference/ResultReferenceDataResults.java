package uk.gov.hmcts.opal.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResultReferenceDataResults {
    private Integer count;
    private List<ResultReferenceData> refData;

    public static class ResultReferenceDataResultsBuilder {
        public ResultReferenceDataResults.ResultReferenceDataResultsBuilder refData(
            List<ResultReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private ResultReferenceDataResults.ResultReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
