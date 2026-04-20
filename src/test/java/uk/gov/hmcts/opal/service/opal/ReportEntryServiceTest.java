package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.ReportIdConstants.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.util.ReportIdConstants.TRACK_ENFORCEMENT_HOLD;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private ReportEntryService reportEntryService;

    @Captor
    private ArgumentCaptor<ReportEntryEntity> reportEntryCaptor;

    @Test
    void createRemoveEnforcementHoldReportEntry_savesExpectedReportEntry() {
        Long defendantAccountId = 77L;
        short businessUnitId = 10;
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(businessUnitId)
            .businessUnitName("Test BU")
            .build();

        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);

        final LocalDateTime beforeCall = LocalDateTime.now();
        reportEntryService.createRemoveEnforcementHoldReportEntry(defendantAccountId, businessUnitId);
        final LocalDateTime afterCall = LocalDateTime.now();

        verify(businessUnitService).getBusinessUnit(businessUnitId);
        verify(reportEntryRepository).save(reportEntryCaptor.capture());

        ReportEntryEntity savedEntry = reportEntryCaptor.getValue();
        assertNotNull(savedEntry);
        assertEquals(businessUnit, savedEntry.getBusinessUnit());
        assertEquals(TRACK_ENFORCEMENT_HOLD, savedEntry.getReportId());
        assertEquals(String.valueOf(defendantAccountId), savedEntry.getAssociatedRecordId());
        assertEquals(DEFENDANT_ACCOUNTS, savedEntry.getAssociatedRecordType());
        assertNotNull(savedEntry.getEntryTimestamp());
        assertFalse(savedEntry.getEntryTimestamp().isBefore(beforeCall));
        assertFalse(savedEntry.getEntryTimestamp().isAfter(afterCall));
        assertNull(savedEntry.getReportedTimestamp());
    }
}
