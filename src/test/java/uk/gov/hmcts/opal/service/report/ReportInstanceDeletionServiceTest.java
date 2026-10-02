package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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

    @Mock
    private ReportInstanceRepository reportInstanceRepository;

    @Mock
    private ReportBlobStore reportBlobStore;

    @InjectMocks
    private ReportInstanceDeletionService reportInstanceDeletionService;

    @Test
    void deleteReportInstances_deletesFoundReportInstancesAndStoredContent() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, "location-1");
        ReportInstanceEntity reportInstance2 = reportInstance(2L, "location-2");
        when(reportInstanceRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(reportInstance1, reportInstance2));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 2L));

        verify(reportBlobStore).deleteReport("location-1");
        verify(reportBlobStore).deleteReport("location-2");
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1, reportInstance2));
    }

    @Test
    void deleteReportInstances_deduplicatesRepeatedIdsAndLocations() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, "location-1");
        ReportInstanceEntity reportInstance2 = reportInstance(2L, "location-1");
        when(reportInstanceRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(reportInstance1, reportInstance2));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 1L, 2L));

        verify(reportBlobStore).deleteReport("location-1");
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1, reportInstance2));
    }

    @Test
    void deleteReportInstances_ignoresMissingIds() {
        ReportInstanceEntity reportInstance = reportInstance(1L, "location-1");
        when(reportInstanceRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(reportInstance));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 99L));

        verify(reportBlobStore).deleteReport("location-1");
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
    void deleteReportInstances_skipsBlankAndNullLocations() {
        ReportInstanceEntity reportInstance1 = reportInstance(1L, null);
        ReportInstanceEntity reportInstance2 = reportInstance(2L, " ");
        when(reportInstanceRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(reportInstance1, reportInstance2));

        reportInstanceDeletionService.deleteReportInstances(List.of(1L, 2L));

        verify(reportBlobStore, never()).deleteReport(any());
        verify(reportInstanceRepository).deleteAll(List.of(reportInstance1, reportInstance2));
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

    private ReportInstanceEntity reportInstance(Long id, String location) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportInstanceId(id);
        reportInstance.setLocation(location);
        return reportInstance;
    }
}
