package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interface Job Processed File Summary Service Tests")
class InterfaceJobProcessedFileSummaryServiceTest {

    private static final Long JOB_ID = 257701L;
    private static final Long FILE_ID = 25770101L;
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
    @Captor
    private ArgumentCaptor<List<InterfaceJobsMessageGroup>> messageGroupsCaptor;

    private final UserStateV2 userState = UserStateV2.builder().userId(1L).build();
    private InterfaceJobProcessedFileSummaryService service;

    @BeforeEach
    void setUp() {
        service = new InterfaceJobProcessedFileSummaryService(interfaceJobRepository,
            messageRepository, processedFileSummaryMapper, interfaceMessageMapper, userStateService,
            pdplLoggingService);
    }

    @ParameterizedTest
    @EnumSource(value = TillStatusEnum.class, names = {"Created", "Processing", "Failed"})
    @DisplayName("PO-11406 returns and logs summaries for tills awaiting allocation")
    void returnsMappedSummaryAndLogsView(TillStatusEnum tillStatus) {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, tillStatus);
        stubUserWithPermission();
        InterfaceJobsProcessedFileSummaryResponse mappedResponse = new InterfaceJobsProcessedFileSummaryResponse()
            .fileName("payments.dat");
        when(processedFileSummaryMapper.toResponse(eq(file), eq("Luton"), eq(0L), any()))
            .thenReturn(mappedResponse);

        assertSame(mappedResponse, service.getProcessedFileSummary(JOB_ID));

        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-11406 rejects an allocated till before reading or logging interface-message data")
    void allocatedTillIsRejectedBeforeMessagesOrPdpl() {
        stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Allocated);
        stubPermission();

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verify(userStateService, never()).getUserStateFromSecurityContext();
        verifyNoInteractions(messageRepository, interfaceMessageMapper, processedFileSummaryMapper, pdplLoggingService);
    }

    @Test
    @DisplayName("PO-11406 returns and logs an ignored-file summary without an associated till")
    void ignoredFileWithoutTillRemainsAvailable() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.IGNORED, null);
        stubUserWithPermission();
        when(processedFileSummaryMapper.toResponse(eq(file), eq("Luton"), eq(0L), any()))
            .thenReturn(new InterfaceJobsProcessedFileSummaryResponse().fileName("ignored-file.dat"));

        assertEquals("ignored-file.dat", service.getProcessedFileSummary(JOB_ID).getFileName());

        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-11406 rechecks till allocation status for a previously viewed interface job")
    void allocatedTillCannotBeViewedAgain() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Created);
        stubUserWithPermission();
        when(processedFileSummaryMapper.toResponse(eq(file), eq("Luton"), eq(0L), any()))
            .thenReturn(new InterfaceJobsProcessedFileSummaryResponse().fileName("payments.dat"));

        assertEquals("payments.dat", service.getProcessedFileSummary(JOB_ID).getFileName());
        file.getTillEntities().getFirst().setStatus(TillStatusEnum.Allocated);
        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verify(interfaceJobRepository, times(2)).findById(JOB_ID);
        verify(messageRepository)
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(FILE_ID);
        verify(pdplLoggingService).logView(userState, List.of());
    }

    @Test
    @DisplayName("PO-2576 groups messages by text and counts only warnings and exceptions")
    void groupsMessagesAndCountsErrors() {
        final InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Created);
        stubUserWithPermission();
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

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(file), eq("Luton"), eq(2L), messageGroupsCaptor.capture());
        List<InterfaceJobsMessageGroup> groups = messageGroupsCaptor.getValue();
        assertEquals(2, groups.size());
        assertEquals("records_read", groups.getFirst().getMessageText());
        assertEquals(List.of(1L, 2L), messageIds(groups.getFirst()));
        assertEquals("records_rejected", groups.get(1).getMessageText());
        assertEquals(List.of(3L, 4L), messageIds(groups.get(1)));
        verify(pdplLoggingService).logView(userState, List.of(1L, 2L, 3L, 4L));
    }

    @Test
    void usesTillBusinessUnitNameWhenPresent() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Created);
        file.getTillEntities().getFirst().setBusinessUnit(BusinessUnitEntity.builder()
            .businessUnitId(BUSINESS_UNIT_ID).businessUnitName("Till business unit").build());
        stubUserWithPermission();

        service.getProcessedFileSummary(JOB_ID);

        verify(processedFileSummaryMapper).toResponse(eq(file), eq("Till business unit"), eq(0L), any());
    }

    @Test
    void rejectsMultipleFiles() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, null);
        file.getInterfaceJob().setInterfaceFiles(List.of(file, InterfaceFileEntity.builder().build()));

        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, messageRepository, pdplLoggingService);
    }

    @Test
    void rejectsMultipleTills() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Created);
        file.setTillEntities(List.of(file.getTillEntities().getFirst(), TillEntity.builder().build()));

        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, messageRepository, pdplLoggingService);
    }

    @ParameterizedTest
    @EnumSource(value = InterfaceJobStatus.class, names = {"CREATED", "PROCESSING", "FAILED"})
    void rejectsJobsWithoutProcessedSummary(InterfaceJobStatus status) {
        stubFile(status, null);

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, messageRepository, pdplLoggingService);
    }

    @Test
    void missingJobThrowsNotFoundWithoutPermissionOrPdpl() {
        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, pdplLoggingService, messageRepository);
    }

    @Test
    void missingFileThrowsNotFoundWithoutPermissionOrPdpl() {
        InterfaceFileEntity file = stubFile(InterfaceJobStatus.COMPLETED, null);
        file.getInterfaceJob().setInterfaceFiles(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, pdplLoggingService, messageRepository);
    }

    @Test
    @DisplayName("PO-2576 rejects users without permission before reading messages")
    void userWithoutBusinessUnitPermissionIsRejectedBeforeMessagesOrPdpl() {
        stubFile(InterfaceJobStatus.COMPLETED, TillStatusEnum.Allocated);

        assertThrows(PermissionNotAllowedException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(messageRepository, processedFileSummaryMapper, pdplLoggingService);
    }

    private InterfaceFileEntity stubFile(InterfaceJobStatus status, TillStatusEnum tillStatus) {
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID)
            .businessUnitName("Luton").build();
        InterfaceJobEntity job = InterfaceJobEntity.builder().interfaceJobId(JOB_ID)
            .businessUnit(businessUnit).status(status).build();
        InterfaceFileEntity file = InterfaceFileEntity.builder().interfaceFileId(FILE_ID)
            .interfaceJob(job).fileName("payments.dat").build();
        if (tillStatus != null) {
            file.setTillEntities(List.of(TillEntity.builder().status(tillStatus).businessUnit(businessUnit).build()));
        }
        job.setInterfaceFiles(List.of(file));
        when(interfaceJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
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

    private InterfaceMessageEntity message(Long id, String text, String type) {
        return InterfaceMessageEntity.builder().interfaceMessageId(id).messageText(text).messageType(type).build();
    }

    private List<Long> messageIds(InterfaceJobsMessageGroup group) {
        return group.getMessages().stream().map(InterfaceJobsMessage::getInterfaceMessagesId).toList();
    }
}
