package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerLegacyEntity;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

        EnforcerLegacyEntity enforcerLegacyEntity = EnforcerLegacyEntity.builder().build();
        when(enforcerRepository.findById(any())).thenReturn(Optional.of(enforcerLegacyEntity));

        // Act
        EnforcerLegacyEntity result = enforcerService.getEnforcerById(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchEnforcers() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        EnforcerLegacyEntity enforcerLegacyEntity = EnforcerLegacyEntity.builder().build();
        Page<EnforcerLegacyEntity> mockPage = new PageImpl<>(List.of(enforcerLegacyEntity), Pageable.unpaged(), 999L);

        when(enforcerRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<EnforcerLegacyEntity> result = enforcerService.searchEnforcers(EnforcerSearchDto.builder().build());

        // Assert
        assertEquals(List.of(enforcerLegacyEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testEnforcersReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        EnforcerLegacyEntity enforcerLegacyEntity = EnforcerLegacyEntity.builder().build();
        Page<EnforcerLegacyEntity> mockPage = new PageImpl<>(List.of(enforcerLegacyEntity), Pageable.unpaged(), 999L);

        when(enforcerRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<EnforcerReferenceData> result = enforcerService.getReferenceData(Optional.empty());

        EnforcerReferenceData refData =  new EnforcerReferenceData(
            enforcerLegacyEntity.getEnforcerId(),
            enforcerLegacyEntity.getEnforcerCode(),
            enforcerLegacyEntity.getName(),
            enforcerLegacyEntity.getNameCy()
        );

        // Assert
        assertEquals(List.of(refData), result);

    }
}
