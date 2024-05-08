package uk.gov.hmcts.opal.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourtReferenceDataResults {
    private Integer count;
    private List<CourtReferenceData> refData;

    public static class CourtReferenceDataResultsBuilder {
        public CourtReferenceDataResultsBuilder refData(
            List<CourtReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private CourtReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
