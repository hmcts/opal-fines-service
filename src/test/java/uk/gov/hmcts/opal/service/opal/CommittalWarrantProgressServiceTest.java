package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCommittalWarrantProgresss() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        CommittalWarrantProgressEntity committalWarrantProgressEntity =
            CommittalWarrantProgressEntity.builder().build();
        Page<CommittalWarrantProgressEntity> mockPage = new PageImpl<>(List.of(committalWarrantProgressEntity),
                                                                       Pageable.unpaged(), 999L);
        when(committalWarrantProgressRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<CommittalWarrantProgressEntity> result = committalWarrantProgressService
            .searchCommittalWarrantProgresss(CommittalWarrantProgressSearchDto.builder().build());

        // Assert
        assertEquals(List.of(committalWarrantProgressEntity), result);

    }


}
