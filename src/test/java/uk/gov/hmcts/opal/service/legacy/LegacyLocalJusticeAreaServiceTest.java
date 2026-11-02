package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.mapper.LocalJusticeAreaLegacyMapper;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;

@ExtendWith(MockitoExtension.class)
class LegacyLocalJusticeAreaServiceTest {

    @Mock
    private LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

    @Mock
    private LocalJusticeAreaLegacyMapper localJusticeAreaLegacyMapper;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private LegacyLocalJusticeAreaService service;

    @Test
    void getLocalJusticeAreaById_mapsLegacyEntity() {
        LocalJusticeAreaLegacyEntity entity = LocalJusticeAreaLegacyEntity.builder()
            .localJusticeAreaId((short) 1)
            .build();
        LocalJusticeArea expected = LocalJusticeArea.builder().localJusticeAreaId((short) 1).build();
        when(localJusticeAreaLegacyRepository.findById((short) 1)).thenReturn(Optional.of(entity));
        when(localJusticeAreaLegacyMapper.toLocalJusticeArea(entity)).thenReturn(expected);

        LocalJusticeArea result = service.getLocalJusticeAreaById((short) 1);

        assertEquals(expected, result);
        verify(localJusticeAreaLegacyMapper).toLocalJusticeArea(entity);
    }

    @Test
    void getLocalJusticeAreaEntityById_retainsLegacyOnlyFields() {
        LocalJusticeAreaLegacyEntity entity = LocalJusticeAreaLegacyEntity.builder()
            .localJusticeAreaId((short) 1)
            .ljaCode("0007")
            .build();
        when(localJusticeAreaLegacyRepository.findById((short) 1)).thenReturn(Optional.of(entity));

        LocalJusticeAreaLegacyEntity result = service.getLocalJusticeAreaEntityById((short) 1);

        assertEquals("0007", result.getLjaCode());
    }
}
