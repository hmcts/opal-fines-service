package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobProcessedFileSummaryMapper;
import uk.gov.hmcts.opal.mapper.InterfaceMessageMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobsProcessedFileSummaryRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interface Job Processed File Summary Service Tests")
class InterfaceJobProcessedFileSummaryServiceTest {

    private static final Long JOB_ID = 257701L;
    private static final Long FILE_ID = 25770101L;
    private static final Long TILL_ID = 25770102L;
    private static final Short BUSINESS_UNIT_ID = 2577;

    @Mock
    private InterfaceJobRepository interfaceJobRepository;
    @Mock
    private InterfaceJobProcessedFileSummaryMapper processedFileSummaryMapper;
    @Mock
    private InterfaceMessageMapper interfaceMessageMapper;
    @Mock
    private UserStateService userStateService;
    @Mock
    private InterfaceMessageRepository messageRepository;
    @Mock
    private InterfaceJobProcessedFileSummaryPdplLoggingService pdplLoggingService;
    @Mock
    private InterfaceJobsProcessedFileSummaryRepository summaryViewRepository;
    @Mock
    private TillRepository tillRepository;
    @Captor
    private ArgumentCaptor<List<InterfaceJobsMessageGroup>> messageGroupsCaptor;

    private final UserStateV2 userState = UserStateV2.builder().userId(1L).build();
    private InterfaceJobProcessedFileSummaryService service;

    @BeforeEach
    void setUp() {
        service = new InterfaceJobProcessedFileSummaryService(interfaceJobRepository,
            messageRepository, processedFileSummaryMapper, interfaceMessageMapper, userStateService,
            pdplLoggingService, summaryViewRepository, tillRepository);
    }

    @ParameterizedTest
    @EnumSource(value = TillStatusEnum.class, names = {"Created", "Processing", "Failed"})
    @DisplayName("PO-11406 returns completed summaries from the view for tills awaiting allocation")
    void returnsCompletedViewSummary(TillStatusEnum tillStatus) {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubViewWithDetails();
        stubReferencedTill(summary, tillStatus);
        stubUserWithPermission();
        InterfaceJobsProcessedFileSummaryResponse mappedResponse = new InterfaceJobsProcessedFileSummaryResponse()
            .fileName("payments.dat");
        when(processedFileSummaryMapper.toResponse(eq(summary), eq("Luton"), any())).thenReturn(mappedResponse);

        assertSame(mappedResponse, service.getProcessedFileSummary(JOB_ID));

        verify(tillRepository).findById(TILL_ID);
        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-11406 rejects an allocated till before reading or logging interface-message data")
    void allocatedTillIsRejectedBeforeMessagesOrPdpl() {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubView();
        stubReferencedTill(summary, TillStatusEnum.Allocated);
        stubPermission();

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verify(userStateService, never()).getUserStateFromSecurityContext();
        verifyNoInteractions(messageRepository, interfaceMessageMapper, processedFileSummaryMapper, pdplLoggingService);
    }

    @Test
    void missingReferencedTillIsRejectedBeforeMessagesOrPdpl() {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubView();
        when(summary.getTillId()).thenReturn(TILL_ID);
        stubPermission();

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(messageRepository, interfaceMessageMapper, processedFileSummaryMapper, pdplLoggingService);
    }

    @Test
    void completedSummaryWithoutTillRemainsAvailable() {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubViewWithDetails();
        when(summary.getTillId()).thenReturn(null);
        stubUserWithPermission();
        when(processedFileSummaryMapper.toResponse(eq(summary), eq("Luton"), any()))
            .thenReturn(new InterfaceJobsProcessedFileSummaryResponse().fileName("payments.dat"));

        assertEquals("payments.dat", service.getProcessedFileSummary(JOB_ID).getFileName());

        verifyNoInteractions(tillRepository);
        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-11406 returns an ignored-file summary without querying the completed view or tills")
    void ignoredFileWithoutTillRemainsAvailable() {
        InterfaceFileEntity file = stubIgnoredFile(null);
        stubUserWithPermission();
        when(processedFileSummaryMapper.toResponse(eq(file), eq("Luton"), eq(0L), any()))
            .thenReturn(new InterfaceJobsProcessedFileSummaryResponse().fileName("ignored-file.dat"));

        assertEquals("ignored-file.dat", service.getProcessedFileSummary(JOB_ID).getFileName());

        verifyNoInteractions(summaryViewRepository, tillRepository);
        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-11406 rechecks till allocation status for a previously viewed interface job")
    void allocatedTillCannotBeViewedAgain() {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubViewWithDetails();
        when(summary.getTillId()).thenReturn(TILL_ID);
        when(tillRepository.findById(TILL_ID))
            .thenReturn(Optional.of(TillEntity.builder().status(TillStatusEnum.Created).build()))
            .thenReturn(Optional.of(TillEntity.builder().status(TillStatusEnum.Allocated).build()));
        stubUserWithPermission();
        when(processedFileSummaryMapper.toResponse(eq(summary), eq("Luton"), any()))
            .thenReturn(new InterfaceJobsProcessedFileSummaryResponse().fileName("payments.dat"));

        assertEquals("payments.dat", service.getProcessedFileSummary(JOB_ID).getFileName());
        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verify(summaryViewRepository, times(2)).findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID);
        verify(tillRepository, times(2)).findById(TILL_ID);
        verify(messageRepository)
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(FILE_ID);
        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-2576 groups completed-view messages while retaining every source row")
    void groupsCompletedViewMessages() {
        stubJob(InterfaceJobStatus.COMPLETED);
        final InterfaceJobProcessedFileSummaryEntity summary = stubViewWithDetails();
        when(summary.getTillId()).thenReturn(null);
        stubUserWithPermission();
        stubMessages();

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(summary), eq("Luton"), messageGroupsCaptor.capture());
        assertMessageGroups();
        verify(pdplLoggingService).logView(userState, List.of(1L, 2L, 3L, 4L));
    }

    @Test
    void groupsIgnoredFileMessagesAndCountsOnlyWarningsAndExceptions() {
        final InterfaceFileEntity file = stubIgnoredFile(null);
        stubUserWithPermission();
        stubMessages();

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(file), eq("Luton"), eq(2L), messageGroupsCaptor.capture());
        assertMessageGroups();
        verifyNoInteractions(summaryViewRepository, tillRepository);
        verify(pdplLoggingService).logView(userState, List.of(1L, 2L, 3L, 4L));
    }

    @Test
    void missingViewBusinessUnitNameFallsBackToJob() {
        stubJob(InterfaceJobStatus.COMPLETED);
        InterfaceJobProcessedFileSummaryEntity summary = stubView();
        when(summary.getInterfaceFileId()).thenReturn(FILE_ID);
        when(summary.getTillId()).thenReturn(null);
        stubUserWithPermission();

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(summary), eq("Luton"), any());
        verifyNoInteractions(tillRepository);
    }

    @Test
    void ignoredFileUsesTillBusinessUnitNameWhenPresent() {
        InterfaceFileEntity file = stubIgnoredFile(TillStatusEnum.Created);
        file.getTillEntities().getFirst().setBusinessUnit(BusinessUnitEntity.builder()
            .businessUnitId(BUSINESS_UNIT_ID).businessUnitName("Till business unit").build());
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(file.getTillEntities().getFirst()));
        stubUserWithPermission();

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(file), eq("Till business unit"), eq(0L), any());
    }

    @Test
    void rejectsMultipleViewRows() {
        stubJob(InterfaceJobStatus.COMPLETED);
        when(summaryViewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID))
            .thenReturn(List.of(mock(InterfaceJobProcessedFileSummaryEntity.class),
                mock(InterfaceJobProcessedFileSummaryEntity.class)));

        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(tillRepository, messageRepository, pdplLoggingService);
    }

    @Test
    void rejectsMultipleIgnoredFiles() {
        InterfaceFileEntity file = stubIgnoredFile(null);
        file.getInterfaceJob().setInterfaceFiles(List.of(file, InterfaceFileEntity.builder().build()));

        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(summaryViewRepository, tillRepository, messageRepository, pdplLoggingService);
    }

    @Test
    void rejectsMultipleIgnoredTills() {
        InterfaceFileEntity file = stubIgnoredFile(TillStatusEnum.Created);
        file.setTillEntities(List.of(file.getTillEntities().getFirst(), TillEntity.builder().build()));

        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(summaryViewRepository, tillRepository, messageRepository, pdplLoggingService);
    }

    @ParameterizedTest
    @EnumSource(value = InterfaceJobStatus.class, names = {"CREATED", "PROCESSING", "FAILED"})
    void rejectsJobsWithoutProcessedSummary(InterfaceJobStatus status) {
        stubJob(status);

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(summaryViewRepository, tillRepository, messageRepository, pdplLoggingService);
    }

    @Test
    void missingJobThrowsNotFoundWithoutPermissionOrPdpl() {
        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(summaryViewRepository, tillRepository, userStateService, pdplLoggingService,
            messageRepository);
    }

    @Test
    void missingCompletedViewThrowsNotFound() {
        stubJob(InterfaceJobStatus.COMPLETED);

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(tillRepository, pdplLoggingService, messageRepository);
    }

    @Test
    void missingIgnoredFileThrowsNotFound() {
        stubJob(InterfaceJobStatus.IGNORED);

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(summaryViewRepository, tillRepository, pdplLoggingService, messageRepository);
    }

    @Test
    @DisplayName("PO-2576 checks business-unit permission before looking up a referenced till or messages")
    void userWithoutBusinessUnitPermissionIsRejectedBeforeTillLookup() {
        stubJob(InterfaceJobStatus.COMPLETED);
        stubView();

        assertThrows(PermissionNotAllowedException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(tillRepository, messageRepository, processedFileSummaryMapper, pdplLoggingService);
    }

    private InterfaceJobEntity stubJob(InterfaceJobStatus status) {
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID)
            .businessUnitName("Luton").build();
        InterfaceJobEntity job = InterfaceJobEntity.builder().interfaceJobId(JOB_ID)
            .businessUnit(businessUnit).status(status).build();
        when(interfaceJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        return job;
    }

    private InterfaceJobProcessedFileSummaryEntity stubView() {
        InterfaceJobProcessedFileSummaryEntity summary = mock(InterfaceJobProcessedFileSummaryEntity.class);
        when(summaryViewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID))
            .thenReturn(List.of(summary));
        return summary;
    }

    private InterfaceJobProcessedFileSummaryEntity stubViewWithDetails() {
        InterfaceJobProcessedFileSummaryEntity summary = stubView();
        when(summary.getInterfaceFileId()).thenReturn(FILE_ID);
        when(summary.getBusinessUnitName()).thenReturn("Luton");
        return summary;
    }

    private void stubReferencedTill(InterfaceJobProcessedFileSummaryEntity summary, TillStatusEnum tillStatus) {
        when(summary.getTillId()).thenReturn(TILL_ID);
        when(tillRepository.findById(TILL_ID))
            .thenReturn(Optional.of(TillEntity.builder().tillId(TILL_ID).status(tillStatus).build()));
    }

    private InterfaceFileEntity stubIgnoredFile(TillStatusEnum tillStatus) {
        InterfaceJobEntity job = stubJob(InterfaceJobStatus.IGNORED);
        InterfaceFileEntity file = InterfaceFileEntity.builder().interfaceFileId(FILE_ID)
            .interfaceJob(job).fileName("payments.dat").build();
        if (tillStatus != null) {
            TillEntity till = TillEntity.builder().tillId(TILL_ID).status(tillStatus)
                .businessUnit(job.getBusinessUnit()).build();
            file.setTillEntities(List.of(till));
        }
        job.setInterfaceFiles(List.of(file));
        return file;
    }

    private void stubPermission() {
        when(userStateService.getPermittedBusinessUnitIds(List.of(BUSINESS_UNIT_ID),
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
    }

    private void stubUserWithPermission() {
        stubPermission();
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
    }

    private void stubMessages() {
        List<InterfaceMessageEntity> messages = List.of(
            message(1L, "records_read", "Info"), message(2L, "records_read", "Warning"),
            message(3L, "records_rejected", "Exception"), message(4L, "records_rejected", "Error"));
        when(messageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(FILE_ID))
            .thenReturn(messages);
        for (InterfaceMessageEntity message : messages) {
            when(interfaceMessageMapper.toMessage(message)).thenReturn(new InterfaceJobsMessage()
                .interfaceMessagesId(message.getInterfaceMessageId()));
        }
    }

    private InterfaceMessageEntity message(Long id, String text, String type) {
        return InterfaceMessageEntity.builder().interfaceMessageId(id).messageText(text).messageType(type).build();
    }

    private void assertMessageGroups() {
        List<InterfaceJobsMessageGroup> groups = messageGroupsCaptor.getValue();
        assertEquals(2, groups.size());
        assertEquals("records_read", groups.getFirst().getMessageText());
        assertEquals(List.of(1L, 2L), messageIds(groups.getFirst()));
        assertEquals("records_rejected", groups.get(1).getMessageText());
        assertEquals(List.of(3L, 4L), messageIds(groups.get(1)));
    }

    private List<Long> messageIds(InterfaceJobsMessageGroup group) {
        return group.getMessages().stream().map(InterfaceJobsMessage::getInterfaceMessagesId).toList();
    }
}
