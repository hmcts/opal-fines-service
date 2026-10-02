package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@ExtendWith(MockitoExtension.class)
class ReportInstanceDeletionServiceTest {

    private static final UUID LOCATION_1 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID LOCATION_2 = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Mock
    private ReportInstanceRepository reportInstanceRepository;

    @Mock
    private ReportBlobStore reportBlobStore;

    @InjectMocks
    private ReportInstanceDeletionService reportInstanceDeletionService;

    @Test
    void deleteReportInstances_deletesFoundReportInstancesAndStoredContent() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, LOCATION_1);
        ReportInstanceEntity reportInstance2 = reportInstance(2L, LOCATION_2);
        when(reportInstanceRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(reportInstance1, reportInstance2));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 2L));

        verify(reportBlobStore).deleteReport(LOCATION_1);
        verify(reportBlobStore).deleteReport(LOCATION_2);
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1, reportInstance2));
    }

    @Test
    void deleteReportInstances_deduplicatesRepeatedIdsAndLocations() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, LOCATION_1);
        ReportInstanceEntity reportInstance2 = reportInstance(2L, LOCATION_1);
        when(reportInstanceRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(reportInstance1, reportInstance2));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 1L, 2L));

        verify(reportBlobStore).deleteReport(LOCATION_1);
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1, reportInstance2));
    }

    @Test
    void deleteReportInstances_ignoresMissingIds() {
        ReportInstanceEntity reportInstance = reportInstance(1L, LOCATION_1);
        when(reportInstanceRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(reportInstance));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 99L));

        verify(reportBlobStore).deleteReport(LOCATION_1);
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance));
    }

    @Test
    void deleteReportInstances_doesNotFailWhenNoIdsExist() {
        when(reportInstanceRepository.findAllById(List.of(99L))).thenReturn(List.of());

        reportInstanceDeletionService.deleteReportInstances(List.of(99L));

        verifyNoInteractions(reportBlobStore);
        verify(reportInstanceRepository).deleteAll(List.of());
    }

    @Test
    void deleteReportInstances_skipsNullLocations() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, null);
        when(reportInstanceRepository.findAllById(List.of(1L)))
            .thenReturn(List.of(reportInstance1));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L));

        verify(reportBlobStore, never()).deleteReport(any());
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1));
    }

    @Test
    void deleteReportInstances_whenIdsAreEmpty_throwsInvalidReferenceValidationException() {
        assertThrows(InvalidReferenceValidationException.class,
            () -> reportInstanceDeletionService.deleteReportInstances(List.of()));

        verifyNoInteractions(reportInstanceRepository, reportBlobStore);
    }

    @Test
    void deleteReportInstances_whenIdsContainNull_throwsInvalidReferenceValidationException() {
        List<Long> reportInstanceIds = new java.util.ArrayList<>();
        reportInstanceIds.add(null);

        assertThrows(InvalidReferenceValidationException.class,
            () -> reportInstanceDeletionService.deleteReportInstances(reportInstanceIds));

        verifyNoInteractions(reportInstanceRepository, reportBlobStore);
    }

    private ReportInstanceEntity reportInstance(Long id, UUID location) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportInstanceId(id);
        reportInstance.setLocation(location);
        return reportInstance;
    }
}
