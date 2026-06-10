package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.RecordTypeConstants.PAYMENT_TERMS;
import static uk.gov.hmcts.opal.util.ReportIdConstants.LIST_EXTEND_TTP;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-22T09:15:00Z"), ZoneOffset.UTC);

    @Test
    void createExtendTtpReportEntry_savesExpectedReportEntry() {
        Long paymentTermsId = 77L;
        short businessUnitId = 10;
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(businessUnitId)
            .businessUnitName("Test BU")
            .build();

        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);

        reportEntryService.createExtendTtpReportEntry(paymentTermsId, businessUnitId);

        verify(businessUnitService).getBusinessUnit(businessUnitId);
        verify(reportEntryRepository).save(reportEntryCaptor.capture());

        ReportEntryEntity savedEntry = reportEntryCaptor.getValue();
        assertNotNull(savedEntry);
        assertEquals(businessUnit, savedEntry.getBusinessUnit());
        assertEquals(LIST_EXTEND_TTP, savedEntry.getReportId());
        assertEquals(String.valueOf(paymentTermsId), savedEntry.getAssociatedRecordId());
        assertEquals(PAYMENT_TERMS, savedEntry.getAssociatedRecordType());
        assertEquals(LocalDateTime.of(2026, 4, 22, 9, 15), savedEntry.getEntryTimestamp());
        assertNull(savedEntry.getReportedTimestamp());
    }
}
