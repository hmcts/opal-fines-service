package uk.gov.hmcts.opal.mapper.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItemTestingSupport;

public class TestingSupportInterfaceJobMapperTest {
    private final TestingSupportInterfaceJobMapper mapper = Mappers.getMapper(TestingSupportInterfaceJobMapper.class);

    @Test
    void toCreateResponseItem_mapsAllFields() {
        // Arrange
        long fileId = 110L;
        long jobId = 220L;
        InterfaceFileEntity file = mock(InterfaceFileEntity.class);
        when(file.getInterfaceFileId()).thenReturn(fileId);
        InterfaceJobEntity job = mock(InterfaceJobEntity.class);
        when(job.getInterfaceJobId()).thenReturn(jobId);
        when(job.getInterfaceFiles()).thenReturn(List.of(file));

        // Act
        InterfaceJobsCreateResponseItemTestingSupport responseItem = mapper.toCreateResponseItem(job);

        // Assert
        assertThat(responseItem.getInterfaceFileId()).isEqualTo(fileId);
        assertThat(responseItem.getInterfaceJobId()).isEqualTo(jobId);
    }
}
