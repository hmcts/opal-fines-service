package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyCommittalWarrantProgressServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyCommittalWarrantProgressService legacyCommittalWarrantProgressService;

    @Test
    void testGetCommittalWarrantProgress() {
        // Arrange

        CommittalWarrantProgressEntity committalWarrantProgressEntity =
            CommittalWarrantProgressEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyCommittalWarrantProgressService.getCommittalWarrantProgress(1)
        );

        // Assert
        assertNotNull(legacyCommittalWarrantProgressService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchCommittalWarrantProgresss() {
        // Arrange

        CommittalWarrantProgressEntity committalWarrantProgressEntity =
            CommittalWarrantProgressEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyCommittalWarrantProgressService
                .searchCommittalWarrantProgresss(CommittalWarrantProgressSearchDto.builder().build())
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
