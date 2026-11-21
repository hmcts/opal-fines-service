package uk.gov.hmcts.opal.dto.reference;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LegacyLjaReferenceDataResults {
    private Integer count;
    private List<LegacyLjaReferenceData> refData;

    public static class LegacyLjaReferenceDataResultsBuilder {
        public LegacyLjaReferenceDataResultsBuilder refData(
            List<LegacyLjaReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private LegacyLjaReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
