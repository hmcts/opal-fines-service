package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobProcessedFileSummaryMapper;
import uk.gov.hmcts.opal.mapper.InterfaceMessageMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobsProcessedFileSummaryRepository;
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
    private InterfaceJobsProcessedFileSummaryRepository viewRepository;
    @Mock
    private InterfaceMessageRepository messageRepository;
    @Mock
    private InterfaceJobProcessedFileSummaryPdplLoggingService pdplLoggingService;
    @Mock
    private UserStateV2 userState;
    @Captor
    private ArgumentCaptor<List<InterfaceJobsMessageGroup>> messageGroupsCaptor;

    private InterfaceJobProcessedFileSummaryService service;

    @BeforeEach
    void setUp() {
        service = new InterfaceJobProcessedFileSummaryService(interfaceJobRepository, viewRepository,
            messageRepository, processedFileSummaryMapper, interfaceMessageMapper, userStateService,
            pdplLoggingService);
    }

    @Test
    @DisplayName("PO-2576 returns a mapped processed-file summary and logs the view")
    void returnsMappedSummaryAndLogsView() {

        // Arrange
        final InterfaceJobProcessedFileSummaryEntity interfaceJobProcessedFileSummary = stubSummaryViewWithDetails();
        stubInterfaceJobWithBusinessUnit();
        stubUserWithPermission();

        when(messageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(FILE_ID))
            .thenReturn(List.of());

        InterfaceJobsProcessedFileSummaryResponse mappedResponse = new InterfaceJobsProcessedFileSummaryResponse();
        when(processedFileSummaryMapper.toResponse(eq(interfaceJobProcessedFileSummary),
            eq("Luton"), any())).thenReturn(mappedResponse);

        // Act
        InterfaceJobsProcessedFileSummaryResponse response = service.getProcessedFileSummary(JOB_ID);

        // Assert
        assertSame(mappedResponse, response);
        verify(pdplLoggingService).logView(userState);
    }

    @Test
    @DisplayName("PO-2576 groups messages by text while retaining every source row")
    void groupsMessagesByText() {

        // Arrange
        final InterfaceJobProcessedFileSummaryEntity summary = stubSummaryViewWithDetails();
        stubInterfaceJobWithBusinessUnit();
        stubUserWithPermission();

        InterfaceMessageEntity firstRead = mock(InterfaceMessageEntity.class);
        InterfaceMessageEntity secondRead = mock(InterfaceMessageEntity.class);
        InterfaceMessageEntity rejected = mock(InterfaceMessageEntity.class);

        when(firstRead.getMessageText()).thenReturn("records_read");
        when(secondRead.getMessageText()).thenReturn("records_read");
        when(rejected.getMessageText()).thenReturn("records_rejected");
        when(messageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(FILE_ID))
            .thenReturn(List.of(firstRead, secondRead, rejected));
        when(interfaceMessageMapper.toMessage(firstRead)).thenReturn(message(1L));
        when(interfaceMessageMapper.toMessage(secondRead)).thenReturn(message(2L));
        when(interfaceMessageMapper.toMessage(rejected)).thenReturn(message(3L));

        // Act
        service.getProcessedFileSummary(JOB_ID);

        // Assert
        verify(processedFileSummaryMapper).toResponse(eq(summary), eq("Luton"), messageGroupsCaptor.capture());

        List<InterfaceJobsMessageGroup> groups = messageGroupsCaptor.getValue();

        assertEquals(2, groups.size());
        assertEquals("records_read", groups.getFirst().getMessageText());
        assertEquals(List.of(1L, 2L), messageIds(groups.getFirst()));
        assertEquals("records_rejected", groups.get(1).getMessageText());
        assertEquals(List.of(3L), messageIds(groups.get(1)));
    }

    @Test
    @DisplayName("PO-2576 rejects multiple summaries for one interface job")
    void rejectsMultipleSummariesForOneInterfaceJob() {

        // Arrange
        InterfaceJobProcessedFileSummaryEntity firstView = mock(
            InterfaceJobProcessedFileSummaryEntity.class);
        InterfaceJobProcessedFileSummaryEntity secondView = mock(
            InterfaceJobProcessedFileSummaryEntity.class);

        when(viewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID))
            .thenReturn(List.of(firstView, secondView));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(messageRepository, pdplLoggingService);
    }

    @Test
    @DisplayName("PO-2576 returns not found when no summary exists")
    void missingViewThrowsNotFoundWithoutPermissionOrPdpo() {

        // Arrange
        when(viewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID)).thenReturn(List.of());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(userStateService, pdplLoggingService, messageRepository);
    }

    @Test
    @DisplayName("PO-2576 rejects users without permission before reading messages")
    void userWithoutBusinessUnitPermissionIsRejectedBeforeMessagesOrPdpo() {

        // Arrange
        stubSummaryView();
        stubInterfaceJobWithBusinessUnit();

        when(userStateService.getPermittedBusinessUnitIds(List.of(BUSINESS_UNIT_ID),
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        // Act & Assert
        assertThrows(PermissionNotAllowedException.class, () -> service.getProcessedFileSummary(JOB_ID));

        verifyNoInteractions(messageRepository, pdplLoggingService);
    }

    private void stubInterfaceJobWithBusinessUnit() {

        when(interfaceJobRepository.findById(JOB_ID)).thenReturn(Optional.of(InterfaceJobEntity.builder()
            .interfaceJobId(JOB_ID)
            .businessUnit(BusinessUnitEntity.builder()
                .businessUnitId(BUSINESS_UNIT_ID)
                .businessUnitName("Luton")
                .build())
            .build()));
    }

    private void stubUserWithPermission() {

        when(userStateService.getPermittedBusinessUnitIds(List.of(BUSINESS_UNIT_ID),
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
    }

    private InterfaceJobProcessedFileSummaryEntity stubSummaryView() {

        InterfaceJobProcessedFileSummaryEntity view = mock(
            InterfaceJobProcessedFileSummaryEntity.class);
        when(viewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(JOB_ID)).thenReturn(List.of(view));
        when(view.getInterfaceJobId()).thenReturn(JOB_ID);
        return view;
    }

    private InterfaceJobProcessedFileSummaryEntity stubSummaryViewWithDetails() {

        InterfaceJobProcessedFileSummaryEntity interfaceJobProcessedFileSummary = stubSummaryView();
        when(interfaceJobProcessedFileSummary.getInterfaceFileId()).thenReturn(FILE_ID);
        when(interfaceJobProcessedFileSummary.getBusinessUnitName()).thenReturn("Luton");
        return interfaceJobProcessedFileSummary;
    }

    private InterfaceJobsMessage message(Long messageId) {
        return new InterfaceJobsMessage()
            .interfaceMessagesId(messageId)
            .messageType(InterfaceJobsMessageType.INFO);
    }

    private List<Long> messageIds(InterfaceJobsMessageGroup group) {
        return group.getMessages().stream()
            .map(InterfaceJobsMessage::getInterfaceMessagesId)
            .toList();
    }

}
