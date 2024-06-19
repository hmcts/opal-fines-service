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
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.repository.EnforcerRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @SuppressWarnings("unchecked")
    @Test
    void testSearchEnforcers() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        EnforcerEntity enforcerEntity = EnforcerEntity.builder().build();
        Page<EnforcerEntity> mockPage = new PageImpl<>(List.of(enforcerEntity), Pageable.unpaged(), 999L);
        when(enforcerRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<EnforcerEntity> result = enforcerService.searchEnforcers(EnforcerSearchDto.builder().build());

        // Assert
        assertEquals(List.of(enforcerEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testEnforcersReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        EnforcerEntity enforcerEntity = EnforcerEntity.builder().build();
        Page<EnforcerEntity> mockPage = new PageImpl<>(List.of(enforcerEntity), Pageable.unpaged(), 999L);
        when(enforcerRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<EnforcerReferenceData> result = enforcerService.getReferenceData(Optional.empty());

        EnforcerReferenceData refData =  new EnforcerReferenceData(
            enforcerEntity.getEnforcerId(),
            enforcerEntity.getEnforcerCode(),
            enforcerEntity.getName(),
            enforcerEntity.getNameCy()
        );

        // Assert
        assertEquals(List.of(refData), result);

    }
}
