package uk.gov.hmcts.opal.disco.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.mapper.OpalLocalJusticeAreaMapper;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.OpalLocalJusticeAreaService;

@ExtendWith(MockitoExtension.class)
class OpalLocalJusticeAreaServiceTest {

    @Mock
    private OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;

    @Mock
    private OpalLocalJusticeAreaMapper opalLocalJusticeAreaMapper;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private OpalLocalJusticeAreaService localJusticeAreaService;

    @Test
    void getLocalJusticeAreaById_mapsEntity() {
        OpalLocalJusticeAreaEntity entity =
            OpalLocalJusticeAreaEntity.builder().localJusticeAreaId((short) 1).build();
        LocalJusticeArea expected = LocalJusticeArea.builder().localJusticeAreaId((short) 1).build();
        when(opalLocalJusticeAreaRepository.findById((short) 1)).thenReturn(Optional.of(entity));
        when(opalLocalJusticeAreaMapper.toLocalJusticeArea(entity)).thenReturn(expected);

        LocalJusticeArea result = localJusticeAreaService.getLocalJusticeAreaById((short) 1);

        assertEquals(expected, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getLocalJusticeAreaRefData_mapsContentAndCount() {
        SpecificationFluentQuery<OpalLocalJusticeAreaEntity> query = mock(SpecificationFluentQuery.class);
        when(query.sortBy(any())).thenReturn(query);
        OpalLocalJusticeAreaEntity entity =
            OpalLocalJusticeAreaEntity.builder().localJusticeAreaId((short) 1).build();
        Page<OpalLocalJusticeAreaEntity> page = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        when(opalLocalJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(query);
            return page;
        });
        LocalJusticeAreaSummary summary = LocalJusticeAreaSummary.builder()
            .localJusticeAreaId((short) 1)
            .build();
        when(opalLocalJusticeAreaMapper.toLocalJusticeAreaSummaries(List.of(entity))).thenReturn(List.of(summary));

        LocalJusticeAreasResponse result = localJusticeAreaService.getLocalJusticeAreaRefData(null, null);

        assertEquals(1, result.getCount());
        assertEquals(List.of(summary), result.getRefData());
        verify(opalLocalJusticeAreaMapper).toLocalJusticeAreaSummaries(List.of(entity));
    }
}
