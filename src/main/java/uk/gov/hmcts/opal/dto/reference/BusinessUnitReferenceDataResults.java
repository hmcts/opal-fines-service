package uk.gov.hmcts.opal.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.projection.BusinessUnitReferenceData;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitReferenceDataResults {
    private Integer count;
    private List<BusinessUnitReferenceData> refData;

    public static class BusinessUnitReferenceDataResultsBuilder {
        public BusinessUnitReferenceDataResults.BusinessUnitReferenceDataResultsBuilder refData(
            List<BusinessUnitReferenceData> refData) {
            this.refData = refData;
            return this.count(Optional.ofNullable(refData).map(List::size).orElse(0));
        }

        private BusinessUnitReferenceDataResults.BusinessUnitReferenceDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
