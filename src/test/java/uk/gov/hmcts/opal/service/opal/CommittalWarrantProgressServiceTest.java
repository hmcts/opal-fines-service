package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommittalWarrantProgressServiceTest {

    @Mock
    private CommittalWarrantProgressRepository committalWarrantProgressRepository;

    @InjectMocks
    private CommittalWarrantProgressService committalWarrantProgressService;

    @Test
    void testGetCommittalWarrantProgress() {
        // Arrange

        CommittalWarrantProgressEntity committalWarrantProgressEntity =
            CommittalWarrantProgressEntity.builder().build();
        when(committalWarrantProgressRepository.getReferenceById(any())).thenReturn(committalWarrantProgressEntity);

        // Act
        CommittalWarrantProgressEntity result = committalWarrantProgressService
            .getCommittalWarrantProgress(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchCommittalWarrantProgresss() {
        // Arrange

        CommittalWarrantProgressEntity committalWarrantProgressEntity =
            CommittalWarrantProgressEntity.builder().build();
        Page<CommittalWarrantProgressEntity> mockPage = new PageImpl<>(List.of(committalWarrantProgressEntity),
                                                                       Pageable.unpaged(), 999L);
        // when(committalWarrantProgressRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<CommittalWarrantProgressEntity> result = committalWarrantProgressService
            .searchCommittalWarrantProgresss(CommittalWarrantProgressSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
