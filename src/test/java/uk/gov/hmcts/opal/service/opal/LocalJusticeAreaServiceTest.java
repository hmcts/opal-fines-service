package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaServiceTest {

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @InjectMocks
    private LocalJusticeAreaService localJusticeAreaService;

    @Test
    void testGetLocalJusticeArea() {
        // Arrange

        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder().build();
        when(localJusticeAreaRepository.getReferenceById(any())).thenReturn(localJusticeAreaEntity);

        // Act
        LocalJusticeAreaEntity result = localJusticeAreaService.getLocalJusticeArea(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchLocalJusticeAreas() {
        // Arrange

        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder().build();
        Page<LocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJusticeAreaEntity),
                                                               Pageable.unpaged(), 999L);
        // when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<LocalJusticeAreaEntity> result = localJusticeAreaService
            .searchLocalJusticeAreas(LocalJusticeAreaSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
