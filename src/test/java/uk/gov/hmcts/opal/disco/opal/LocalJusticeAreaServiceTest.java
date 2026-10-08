package uk.gov.hmcts.opal.disco.opal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.opal.dto.LocalJusticeAreaDto;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.mapper.LocalJusticeAreaMapper;
import uk.gov.hmcts.opal.repository.LegacyJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaServiceTest {

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;
    @Mock
    private LegacyJusticeAreaRepository legacyJusticeAreaRepository;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @Mock
    private LocalJusticeAreaMapper localJusticeAreaMapper;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private LocalJusticeAreaService localJusticeAreaService;

    @BeforeEach
    void setUp() {
        lenient().when(dynamicConfigService.isLegacyMode()).thenReturn(false);
    }

    @Test
    void testGetLocalJusticeArea() {
        // Arrange
        when(localJusticeAreaMapper.toDto(any(LocalJusticeAreaEntity.class)))
            .thenReturn(LocalJusticeAreaDto.builder().build());
        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder()
                .localJusticeAreaId((short)1)
                .name("Test LJA")
                .build();

        when(localJusticeAreaRepository.findById(any())).thenReturn(Optional.of(localJusticeAreaEntity));
        // Act
        LocalJusticeAreaDto result = localJusticeAreaService.getLocalJusticeAreaById((short)1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLocalJusticeAreas() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder().build();
        Page<LocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJusticeAreaEntity),
                                                               Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LocalJusticeAreaEntity> result = localJusticeAreaService
            .searchLocalJusticeAreas(LocalJusticeAreaSearchDto.builder().build());

        // Assert
        assertEquals(List.of(localJusticeAreaEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testLocalJusticeAreasReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaEntity localJAEntity = LocalJusticeAreaEntity.builder().build();
        LjaReferenceData ljaReferenceData = LjaReferenceData.builder().build();
        Page<LocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJAEntity), Pageable.unpaged(), 999L);
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

    @Test
    void testGetLocalJusticeArea_usesOpalRepositoryWhenLegacyModeDisabled() {

        when(dynamicConfigService.isLegacyMode()).thenReturn(false);

        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder()
            .localJusticeAreaId((short) 1)
            .name("Test LJA")
            .build();

        when(localJusticeAreaRepository.findById((short) 1))
            .thenReturn(Optional.of(entity));

        when(localJusticeAreaMapper.toDto(any(LocalJusticeAreaEntity.class)))
            .thenReturn(LocalJusticeAreaDto.builder().build());

        localJusticeAreaService.getLocalJusticeAreaById((short) 1);

        verify(localJusticeAreaRepository).findById((short) 1);
        verifyNoInteractions(legacyJusticeAreaRepository);
    }

    @Test
    void testGetLocalJusticeArea_usesLegacyRepositoryWhenLegacyModeEnabled() {

        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        LocalJusticeAreaLegacyEntity entity = LocalJusticeAreaLegacyEntity.builder()
            .localJusticeAreaId((short) 1)
            .name("Test LJA")
            .build();

        when(legacyJusticeAreaRepository.findById((short) 1))
            .thenReturn(Optional.of(entity));

        when(localJusticeAreaMapper.toDto(any(LocalJusticeAreaLegacyEntity.class)))
            .thenReturn(LocalJusticeAreaDto.builder().build());

        localJusticeAreaService.getLocalJusticeAreaById((short) 1);

        verify(legacyJusticeAreaRepository).findById((short) 1);
        verifyNoInteractions(localJusticeAreaRepository);
    }
}
