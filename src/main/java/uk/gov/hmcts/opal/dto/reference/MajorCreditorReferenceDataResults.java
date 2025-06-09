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
public class MajorCreditorReferenceDataResults {
    private Integer count;
    private List<MajorCreditorReferenceData> refData;

    public static class MajorCreditorReferenceDataResultsBuilder {
        public MajorCreditorReferenceDataResults.MajorCreditorReferenceDataResultsBuilder refData(
            List<MajorCreditorReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private MajorCreditorReferenceDataResults.MajorCreditorReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
