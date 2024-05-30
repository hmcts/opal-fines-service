package uk.gov.hmcts.opal.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnforcerReferenceDataResults {
    private Integer count;
    private List<EnforcerReferenceData> refData;

    public static class EnforcerReferenceDataResultsBuilder {
        public EnforcerReferenceDataResults.EnforcerReferenceDataResultsBuilder refData(
            List<EnforcerReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private EnforcerReferenceDataResults.EnforcerReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
