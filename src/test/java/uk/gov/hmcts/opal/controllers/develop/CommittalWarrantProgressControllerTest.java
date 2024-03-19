package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.CommittalWarrantProgressController;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.service.opal.CommittalWarrantProgressService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommittalWarrantProgressControllerTest {

    @Mock
    private CommittalWarrantProgressService committalWarrantProgressService;

    @InjectMocks
    private CommittalWarrantProgressController committalWarrantProgressController;

    @Test
    void testGetCommittalWarrantProgress_Success() {
        // Arrange
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();

        when(committalWarrantProgressService.getCommittalWarrantProgress(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CommittalWarrantProgressEntity> response = committalWarrantProgressController
            .getCommittalWarrantProgressById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(committalWarrantProgressService, times(1))
            .getCommittalWarrantProgress(any(Long.class));
    }

    @Test
    void testSearchCommittalWarrantProgresss_Success() {
        // Arrange
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();
        List<CommittalWarrantProgressEntity> committalWarrantProgressList = List.of(entity);

        when(committalWarrantProgressService.searchCommittalWarrantProgresss(any()))
            .thenReturn(committalWarrantProgressList);

        // Act
        CommittalWarrantProgressSearchDto searchDto = CommittalWarrantProgressSearchDto.builder().build();
        ResponseEntity<List<CommittalWarrantProgressEntity>> response = committalWarrantProgressController
            .postCommittalWarrantProgresssSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(committalWarrantProgressList, response.getBody());
        verify(committalWarrantProgressService, times(1)).searchCommittalWarrantProgresss(any());
    }

}
