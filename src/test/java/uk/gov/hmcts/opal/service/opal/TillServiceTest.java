package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.TillRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TillServiceTest {

    @Mock
    private TillRepository tillRepository;

    @InjectMocks
    private TillService tillService;

    @Test
    void testGetTill() {
        // Arrange

        TillEntity tillEntity = TillEntity.builder().build();
        when(tillRepository.getReferenceById(any())).thenReturn(tillEntity);

        // Act
        TillEntity result = tillService.getTill(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchTills() {
        // Arrange

        TillEntity tillEntity = TillEntity.builder().build();
        Page<TillEntity> mockPage = new PageImpl<>(List.of(tillEntity), Pageable.unpaged(), 999L);
        // when(tillRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<TillEntity> result = tillService.searchTills(TillSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
