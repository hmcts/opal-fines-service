package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class StoredReportContentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenSerialized_canBeDeserialized_happyPath() throws Exception {
        StoredReportContent storedReportContent = StoredReportContent.builder()
            .reportData(Map.of("rows", 2))
            .reportMetaData(new ReportMetaData(List.of()))
            .build();

        String serialized = objectMapper.writeValueAsString(storedReportContent);
        StoredReportContent actual = objectMapper.readValue(serialized, StoredReportContent.class);

        assertThat(actual).usingRecursiveComparison().isEqualTo(storedReportContent);
    }
}
