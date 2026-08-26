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
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobProcessedFileSummaryMapper;
import uk.gov.hmcts.opal.mapper.InterfaceMessageMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobsProcessedFileSummaryRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class InterfaceJobProcessedFileSummaryService {

    private final InterfaceJobRepository interfaceJobRepository;

    private final InterfaceJobsProcessedFileSummaryRepository summaryViewRepository;

    private final InterfaceMessageRepository interfaceMessageRepository;

    private final InterfaceJobProcessedFileSummaryMapper processedFileSummaryMapper;

    private final InterfaceMessageMapper interfaceMessageMapper;

    private final UserStateService userStateService;

    private final InterfaceJobProcessedFileSummaryPdplLoggingService pdplLoggingService;

    @Transactional(readOnly = true)
    public InterfaceJobsProcessedFileSummaryResponse getProcessedFileSummary(Long interfaceJobId) {

        InterfaceJobProcessedFileSummaryEntity summary = findProcessedFileSummary(interfaceJobId);
        InterfaceJobEntity interfaceJob = findInterfaceJob(summary);
        checkPermission(getBusinessUnitId(interfaceJob));
        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        List<InterfaceJobsMessageGroup> messageGroups = getMessageGroups(summary.getInterfaceFileId());
        InterfaceJobsProcessedFileSummaryResponse response = processedFileSummaryMapper.toResponse(summary,
            resolveBusinessUnitName(summary, interfaceJob), messageGroups);
        pdplLoggingService.logView(userState);

        return response;
    }

    private InterfaceJobProcessedFileSummaryEntity findProcessedFileSummary(Long interfaceJobId) {

        List<InterfaceJobProcessedFileSummaryEntity> rows = summaryViewRepository
            .findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(interfaceJobId);

        if (rows.isEmpty()) {
            throw new EntityNotFoundException("Processed file summary not found for interface job id: "
                + interfaceJobId);
        }
        // An OPAL interface job represents one interface file. Multiple summary rows indicate
        // inconsistent data, so fail rather than return an arbitrary file summary.
        if (rows.size() > 1) {
            throw new IllegalStateException("Multiple processed file summaries found for interface job id: "
                + interfaceJobId);
        }
        return rows.getFirst();
    }

    private InterfaceJobEntity findInterfaceJob(InterfaceJobProcessedFileSummaryEntity summary) {
        return interfaceJobRepository.findById(summary.getInterfaceJobId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Interface job not found for processed file summary: " + summary.getInterfaceJobId()));
    }

    private void checkPermission(Short businessUnitId) {
        if (!userStateService.getPermittedBusinessUnitIds(
            List.of(businessUnitId), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS).contains(businessUnitId)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }
    }

    private Short getBusinessUnitId(InterfaceJobEntity interfaceJob) {
        if (interfaceJob.getBusinessUnit() == null) {
            throw new PermissionNotAllowedException(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }

        return interfaceJob.getBusinessUnit().getBusinessUnitId();
    }

    private String resolveBusinessUnitName(InterfaceJobProcessedFileSummaryEntity summary,
        InterfaceJobEntity interfaceJob) {

        if (summary.getBusinessUnitName() != null) {
            return summary.getBusinessUnitName();
        }

        return interfaceJob.getBusinessUnit() == null ? null : interfaceJob.getBusinessUnit().getBusinessUnitName();
    }

    private List<InterfaceJobsMessageGroup> getMessageGroups(Long interfaceFileId) {

        Map<String, List<InterfaceJobsMessage>> messagesByText = interfaceMessageRepository
            .findAllByInterfaceFile_InterfaceFileIdOrderByMessageTextAscInterfaceMessageIdAsc(interfaceFileId)
            .stream()
            .collect(Collectors.groupingBy(InterfaceMessageEntity::getMessageText, LinkedHashMap::new,
                Collectors.mapping(interfaceMessageMapper::toMessage, Collectors.toList())));

        return messagesByText.entrySet().stream()
            .map(entry -> new InterfaceJobsMessageGroup()
                .messageText(entry.getKey())
                .messages(entry.getValue()))
            .toList();
    }
}
