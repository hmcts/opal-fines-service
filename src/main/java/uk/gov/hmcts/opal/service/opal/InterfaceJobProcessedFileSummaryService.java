package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class InterfaceJobProcessedFileSummaryService {

    private final InterfaceJobRepository interfaceJobRepository;

    private final InterfaceMessageRepository interfaceMessageRepository;

    private final InterfaceJobProcessedFileSummaryMapper processedFileSummaryMapper;

    private final InterfaceMessageMapper interfaceMessageMapper;

    private final UserStateService userStateService;

    private final InterfaceJobProcessedFileSummaryPdplLoggingService pdplLoggingService;

    private final InterfaceJobsProcessedFileSummaryRepository summaryViewRepository;

    private final TillRepository tillRepository;

    @Transactional(readOnly = true)
    public InterfaceJobsProcessedFileSummaryResponse getProcessedFileSummary(Long interfaceJobId) {

        InterfaceJobEntity interfaceJob = findInterfaceJob(interfaceJobId);
        if (interfaceJob.getStatus() == InterfaceJobStatus.IGNORED) {
            return getIgnoredFileSummary(interfaceJob);
        }
        if (interfaceJob.getStatus() != InterfaceJobStatus.COMPLETED) {
            throw new EntityNotFoundException("Processed file summary not found for interface job id: "
                + interfaceJobId);
        }

        InterfaceJobProcessedFileSummaryEntity summary = findProcessedFileSummary(interfaceJobId);
        checkPermission(getBusinessUnitId(interfaceJob));
        if (summary.getTillId() == null) {
            throw new EntityNotFoundException("Till not found for completed interface job id: " + interfaceJobId);
        }
        checkTillStatus(summary.getTillId());

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        List<InterfaceMessageEntity> messages = interfaceMessageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(
                summary.getInterfaceFileId());
        InterfaceJobsProcessedFileSummaryResponse response = processedFileSummaryMapper.toResponse(
            summary, summary.getBusinessUnitName(), getMessageGroups(messages));

        pdplLoggingService.logView(userState, extractInterfaceMessageIds(messages));

        return response;
    }

    private InterfaceJobsProcessedFileSummaryResponse getIgnoredFileSummary(InterfaceJobEntity interfaceJob) {
        InterfaceFileEntity file = findIgnoredFile(interfaceJob);
        checkPermission(getBusinessUnitId(interfaceJob));
        if (!file.getTillEntities().isEmpty()) {
            checkTillStatus(file.getTillEntities().getFirst().getTillId());
        }

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        List<InterfaceMessageEntity> messages = interfaceMessageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(
                file.getInterfaceFileId());

        List<InterfaceJobsMessageGroup> messageGroups = getMessageGroups(messages);
        List<Long> interfaceMessageIds = extractInterfaceMessageIds(messages);
        long totalErrors = messages.stream()
            .filter(message -> "Exception".equals(message.getMessageType())
                || "Warning".equals(message.getMessageType()))
            .count();
        InterfaceJobsProcessedFileSummaryResponse response = processedFileSummaryMapper.toResponse(file,
            interfaceJob.getBusinessUnit().getBusinessUnitName(), totalErrors, messageGroups);

        pdplLoggingService.logView(userState, interfaceMessageIds);

        return response;
    }

    private List<Long> extractInterfaceMessageIds(List<InterfaceMessageEntity> messages) {
        return messages.stream()
            .map(InterfaceMessageEntity::getInterfaceMessageId)
            .toList();
    }

    private InterfaceJobProcessedFileSummaryEntity findProcessedFileSummary(Long interfaceJobId) {
        List<InterfaceJobProcessedFileSummaryEntity> rows = summaryViewRepository
            .findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(interfaceJobId);
        if (rows.isEmpty()) {
            throw new EntityNotFoundException("Processed file summary not found for interface job id: "
                + interfaceJobId);
        }
        if (rows.size() > 1) {
            throw new IllegalStateException("Multiple processed file summaries found for interface job id: "
                + interfaceJobId);
        }
        return rows.getFirst();
    }

    private InterfaceFileEntity findIgnoredFile(InterfaceJobEntity interfaceJob) {
        Long interfaceJobId = interfaceJob.getInterfaceJobId();
        List<InterfaceFileEntity> files = interfaceJob.getInterfaceFiles();
        if (files.isEmpty()) {
            throw new EntityNotFoundException("Processed file summary not found for interface job id: "
                + interfaceJobId);
        }
        // An OPAL interface job represents one file and at most one till.
        if (files.size() > 1 || files.getFirst().getTillEntities().size() > 1) {
            throw new IllegalStateException("Multiple processed file summaries found for interface job id: "
                + interfaceJobId);
        }
        return files.getFirst();
    }

    private InterfaceJobEntity findInterfaceJob(Long interfaceJobId) {
        return interfaceJobRepository.findById(interfaceJobId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Interface job not found for processed file summary: " + interfaceJobId));
    }

    private void checkPermission(Short businessUnitId) {
        if (!userStateService.getPermittedBusinessUnitIds(
            List.of(businessUnitId), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS).contains(businessUnitId)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }
    }

    private void checkTillStatus(Long tillId) {
        TillEntity till = tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Till not found for processed file summary: " + tillId));
        if (till.getStatus() == TillStatusEnum.Allocated) {
            throw new EntityNotFoundException("Processed file summary unavailable for allocated till: " + tillId);
        }
    }

    private Short getBusinessUnitId(InterfaceJobEntity interfaceJob) {
        if (interfaceJob.getBusinessUnit() == null) {
            throw new PermissionNotAllowedException(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }

        return interfaceJob.getBusinessUnit().getBusinessUnitId();
    }

    private List<InterfaceJobsMessageGroup> getMessageGroups(List<InterfaceMessageEntity> messages) {
        Map<String, List<InterfaceJobsMessage>> messagesByText = messages.stream()
            .collect(Collectors.groupingBy(
                InterfaceMessageEntity::getMessageText,
                LinkedHashMap::new,
                Collectors.mapping(interfaceMessageMapper::toMessage, Collectors.toList())));

        return messagesByText.entrySet().stream()
            .map(entry -> new InterfaceJobsMessageGroup()
                .messageText(entry.getKey())
                .messages(entry.getValue()))
            .toList();
    }
}
