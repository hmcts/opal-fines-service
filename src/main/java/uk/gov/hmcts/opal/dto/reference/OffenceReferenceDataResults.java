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
public class OffenceReferenceDataResults {
    private Integer count;
    private List<OffenceReferenceData> refData;

    public static class OffenceReferenceDataResultsBuilder {
        public OffenceReferenceDataResults.OffenceReferenceDataResultsBuilder refData(
            List<OffenceReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private OffenceReferenceDataResults.OffenceReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
