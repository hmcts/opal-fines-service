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
public class OffenceSearchDataResults {
    private Integer count;
    private List<OffenceSearchData> searchData;

    public static class OffenceSearchDataResultsBuilder {
        public OffenceSearchDataResults.OffenceSearchDataResultsBuilder searchData(
            List<OffenceSearchData> searchData) {
            this.searchData = searchData;
            return this.count(Optional.ofNullable(searchData).map(List::size).orElse(0));
        }

        private OffenceSearchDataResults.OffenceSearchDataResultsBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}
