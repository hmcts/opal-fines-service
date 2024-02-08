package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.repository.EnforcerRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnforcerServiceTest {

    @Mock
    private EnforcerRepository enforcerRepository;

    @InjectMocks
    private EnforcerService enforcerService;

    @Test
    void testGetEnforcer() {
        // Arrange

        EnforcerEntity enforcerEntity = EnforcerEntity.builder().build();
        when(enforcerRepository.getReferenceById(any())).thenReturn(enforcerEntity);

        // Act
        EnforcerEntity result = enforcerService.getEnforcer(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchEnforcers() {
        // Arrange

        EnforcerEntity enforcerEntity = EnforcerEntity.builder().build();
        Page<EnforcerEntity> mockPage = new PageImpl<>(List.of(enforcerEntity), Pageable.unpaged(), 999L);
        // when(enforcerRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<EnforcerEntity> result = enforcerService.searchEnforcers(EnforcerSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
