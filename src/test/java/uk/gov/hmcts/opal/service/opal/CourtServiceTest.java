package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    @Test
    void testGetCourt() {
        // Arrange

        CourtEntity courtEntity = CourtEntity.builder().build();
        when(courtRepository.getReferenceById(any())).thenReturn(courtEntity);

        // Act
        CourtEntity result = courtService.getCourt(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchCourts() {
        // Arrange

        CourtEntity courtEntity = CourtEntity.builder().build();
        Page<CourtEntity> mockPage = new PageImpl<>(List.of(courtEntity), Pageable.unpaged(), 999L);
        // when(courtRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<CourtEntity> result = courtService.searchCourts(CourtSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
