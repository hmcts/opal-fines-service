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
public class LjaReferenceDataResults {
    private Integer count;
    private List<LjaReferenceData> refData;

    public static class LjaReferenceDataResultsBuilder {
        public LjaReferenceDataResultsBuilder refData(
            List<LjaReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private LjaReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
