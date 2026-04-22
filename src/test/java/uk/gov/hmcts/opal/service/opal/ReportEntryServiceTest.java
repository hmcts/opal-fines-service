package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.ReportIdConstants.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.util.ReportIdConstants.TRACK_ENFORCEMENT_HOLD;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;

@ExtendWith(MockitoExtension.class)
class ReportEntryServiceTest {

    @Mock
    private ReportEntryRepository reportEntryRepository;

    @Mock
    private BusinessUnitService businessUnitService;

    private ReportEntryService reportEntryService;

    @Captor
    private ArgumentCaptor<ReportEntryEntity> reportEntryCaptor;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-04-22T09:15:00Z"), ZoneOffset.UTC);
        reportEntryService = new ReportEntryService(reportEntryRepository, businessUnitService, fixedClock);
    }

    @Test
    void createRemoveEnforcementHoldReportEntry_savesExpectedReportEntry() {
        Long defendantAccountId = 77L;
        short businessUnitId = 10;
        final LocalDateTime expectedEntryTimestamp = LocalDateTime.of(2026, 4, 22, 9, 15);
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(businessUnitId)
            .businessUnitName("Test BU")
            .build();

        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);

        reportEntryService.createRemoveEnforcementHoldReportEntry(defendantAccountId, businessUnitId);

        verify(businessUnitService).getBusinessUnit(businessUnitId);
        verify(reportEntryRepository).save(reportEntryCaptor.capture());

        ReportEntryEntity savedEntry = reportEntryCaptor.getValue();
        assertNotNull(savedEntry);
        assertEquals(businessUnit, savedEntry.getBusinessUnit());
        assertEquals(TRACK_ENFORCEMENT_HOLD, savedEntry.getReportId());
        assertEquals(String.valueOf(defendantAccountId), savedEntry.getAssociatedRecordId());
        assertEquals(DEFENDANT_ACCOUNTS, savedEntry.getAssociatedRecordType());
        assertEquals(expectedEntryTimestamp, savedEntry.getEntryTimestamp());
        assertNull(savedEntry.getReportedTimestamp());
    }
}
