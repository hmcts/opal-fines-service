package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;

public class ReportInstanceMapperTest {
    private final ReportInstanceMapper reportInstanceMapper = Mappers.getMapper(ReportInstanceMapper.class);

    @Test
    public void toResponseDto_shouldMapFromEntity() {
        ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
            .report(ReportEntity.builder().reportId("REPORT-ID-123").build())
            .reportInstanceId(456L)
            .build();
        CreateReportInstanceResponseReports  responseDto = reportInstanceMapper.toResponseDto(reportInstanceEntity);

        assertNotNull(responseDto);
        assertEquals(456L, responseDto.getReportInstanceId());
    }
}
