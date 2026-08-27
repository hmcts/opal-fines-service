package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LegacyLocalJusticeAreaRepository;

@ExtendWith(MockitoExtension.class)
class LegacyLocalJusticeAreaServiceTest {

    @Mock
    private LegacyLocalJusticeAreaRepository localJusticeAreaRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private LegacyLocalJusticeAreaService localJusticeAreaService;

    @Test
    void testGetLocalJusticeArea() {
        LegacyLocalJusticeAreaEntity localJusticeAreaEntity = LegacyLocalJusticeAreaEntity.builder().build();
        when(localJusticeAreaRepository.findById(any())).thenReturn(Optional.of(localJusticeAreaEntity));

        LegacyLocalJusticeAreaEntity result = localJusticeAreaService.getLocalJusticeAreaById((short) 1);

        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLocalJusticeAreas() {
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LegacyLocalJusticeAreaEntity localJusticeAreaEntity = LegacyLocalJusticeAreaEntity.builder().build();
        Page<LegacyLocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJusticeAreaEntity),
            Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        List<LegacyLocalJusticeAreaEntity> result = localJusticeAreaService
            .searchLocalJusticeAreas(LocalJusticeAreaSearchDto.builder().build());

        assertEquals(List.of(localJusticeAreaEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testLocalJusticeAreasReferenceData() {
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LegacyLocalJusticeAreaEntity localJAEntity = LegacyLocalJusticeAreaEntity.builder().build();
        LjaReferenceData ljaReferenceData = LjaReferenceData.builder().build();
        Page<LegacyLocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJAEntity), Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        List<LjaReferenceData> result = localJusticeAreaService.getReferenceData(
            Optional.empty(), Optional.empty());

        assertEquals(List.of(ljaReferenceData), result);
    }
}
