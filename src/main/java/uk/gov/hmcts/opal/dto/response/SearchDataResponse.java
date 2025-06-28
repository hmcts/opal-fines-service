package uk.gov.hmcts.opal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDataResponse<T> {
    private Integer count;
    private List<T> searchData;

    public static class SearchDataResponseBuilder<T> {
        public SearchDataResponseBuilder<T> searchData(List<T> searchData) {
            this.searchData = searchData;
            return this.count(Optional.ofNullable(searchData).map(List::size).orElse(0));
        }

        private SearchDataResponseBuilder<T> count(Integer count) {
            this.count = count;
            return this;
        }
    }
}


