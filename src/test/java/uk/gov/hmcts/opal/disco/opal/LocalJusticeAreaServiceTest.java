package uk.gov.hmcts.opal.disco.opal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaServiceTest {

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private LocalJusticeAreaService localJusticeAreaService;

    @Test
    void testGetLocalJusticeArea() {
        // Arrange

        LocalJusticeAreaLegacyEntity localJusticeAreaLegacyEntity = LocalJusticeAreaLegacyEntity.builder().build();
        when(localJusticeAreaRepository.findById(any())).thenReturn(Optional.of(localJusticeAreaLegacyEntity));

        // Act
        LocalJusticeAreaLegacyEntity result = localJusticeAreaService.getLocalJusticeAreaById((short)1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLocalJusticeAreas() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaLegacyEntity localJusticeAreaLegacyEntity = LocalJusticeAreaLegacyEntity.builder().build();
        Page<LocalJusticeAreaLegacyEntity> mockPage = new PageImpl<>(List.of(localJusticeAreaLegacyEntity),
                                                               Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LocalJusticeAreaLegacyEntity> result = localJusticeAreaService
            .searchLocalJusticeAreas(LocalJusticeAreaSearchDto.builder().build());

        // Assert
        assertEquals(List.of(localJusticeAreaLegacyEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testLocalJusticeAreasReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaLegacyEntity localJAEntity = LocalJusticeAreaLegacyEntity.builder().build();
        LjaReferenceData ljaReferenceData = LjaReferenceData.builder().build();
        Page<LocalJusticeAreaLegacyEntity> mockPage = new PageImpl<>(List.of(localJAEntity), Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LjaReferenceData> result = localJusticeAreaService.getReferenceData(
            Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(ljaReferenceData), result);

    }

}
