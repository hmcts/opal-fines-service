package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnforcementServiceTest {

    @Mock
    private EnforcementRepository enforcementRepository;

    @InjectMocks
    private EnforcementService enforcementService;

    @Test
    void testGetEnforcement() {
        // Arrange

        EnforcementEntity enforcementEntity = EnforcementEntity.builder().build();
        when(enforcementRepository.getReferenceById(any())).thenReturn(enforcementEntity);

        // Act
        EnforcementEntity result = enforcementService.getEnforcement(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchEnforcements() {
        // Arrange

        EnforcementEntity enforcementEntity = EnforcementEntity.builder().build();
        Page<EnforcementEntity> mockPage = new PageImpl<>(List.of(enforcementEntity), Pageable.unpaged(), 999L);
        // when(enforcementRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<EnforcementEntity> result = enforcementService.searchEnforcements(EnforcementSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
