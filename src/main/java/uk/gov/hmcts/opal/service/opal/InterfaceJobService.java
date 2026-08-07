package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity_;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobMapper;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.jpa.InterfaceJobSpecs;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueuePublisher;

@Service
@RequiredArgsConstructor
public class InterfaceJobService {

    private static final Sort SUMMARY_SORT = Sort.by(
        Sort.Order.asc(InterfaceJobEntity_.BUSINESS_UNIT + "." + BusinessUnitEntity_.BUSINESS_UNIT_NAME),
        Sort.Order.desc(InterfaceJobEntity_.CREATED_DATE_TIME));

    private final InterfaceJobRepository interfaceJobRepository;

    private final InterfaceFileRepository interfaceFileRepository;

    private final InterfaceJobMapper interfaceJobMapper;

    private final BusinessUnitService businessUnitService;

    private final UserStateService userStateService;

    private final InterfaceJobQueuePublisher interfaceJobQueuePublisher;

    private final Clock clock;

    private final InterfaceJobSpecs specs = new InterfaceJobSpecs();

    @Transactional
    public InterfaceJobsCreateResponse create(InterfaceJobsCreateRequest request) {
        return InterfaceJobsCreateResponse.builder()
            .interfaceJobs(request.getInterfaceJobs().stream()
                .map(this::create)
                .toList())
            .build();
    }

    private InterfaceJobsCreateResponseItem create(InterfaceJobsCreateItem request) {
        checkPermission(request.getBusinessUnitId());

        InterfaceJobEntity interfaceJob = interfaceJobMapper.toJobEntity(
            request, businessUnitService.getBusinessUnit(request.getBusinessUnitId()));
        InterfaceJobEntity savedJob = interfaceJobRepository.save(interfaceJob);
        InterfaceFileEntity interfaceFile = interfaceJobMapper.toFileEntity(request, savedJob);

        interfaceFileRepository.save(interfaceFile);

        return interfaceJobMapper.toCreateResponse(savedJob);
    }

    private void checkPermission(Short businessUnitId) {
        if (!userStateService.getPermittedBusinessUnitIds(
            List.of(businessUnitId), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS).contains(businessUnitId)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }
    }

    @Transactional(readOnly = true)
    public InterfaceJobsSummaryResponse getSummary(InterfaceJobSearchCriteria searchCriteria) {

        if (searchCriteria.getPermittedBusinessUnitIds(userStateService).isEmpty()) {
            return InterfaceJobsSummaryResponse.builder().interfaceJobs(List.of()).build();
        }

        Page<InterfaceJobEntity> interfaceJobs = interfaceJobRepository
            .findBy(specs.findBySearchCriteria(searchCriteria),
                ffq -> ffq
                    .sortBy(SUMMARY_SORT)
                    .page(Pageable.unpaged()));

        List<InterfaceJobsSummaryItem> summaries = interfaceJobs.getContent()
            .stream()
            .flatMap(interfaceJob -> toResponses(interfaceJob).stream())
            .toList();

        return InterfaceJobsSummaryResponse.builder().interfaceJobs(summaries).build();
    }

    private List<InterfaceJobsSummaryItem> toResponses(InterfaceJobEntity interfaceJob) {
        return interfaceJob.getInterfaceFiles().stream()
            .map(interfaceFile -> interfaceJobMapper.toSummaryResponse(interfaceJob, interfaceFile))
            .toList();
    }

    @Transactional
    public void process(InterfaceJobsProcessRequest request) {
        List<InterfaceJobsProcessItem> requestedJobs = request.getInterfaceJobs();
        List<Long> requestedIds = requestedJobs.stream()
            .map(InterfaceJobsProcessItem::getInterfaceJobId)
            .toList();

        Map<Long, InterfaceJobEntity> jobsById = loadProcessJobs(requestedIds);
        validateRequestedBusinessUnits(requestedJobs, jobsById);
        validateProcessPermissions(requestedJobs);
        validateProcessStatuses(requestedIds, jobsById);

        LocalDateTime startedDateTime = LocalDateTime.now(clock);
        requestedJobs.forEach(requestedJob -> updateForProcessing(
            requestedJob, jobsById.get(requestedJob.getInterfaceJobId()), startedDateTime));

        interfaceJobQueuePublisher.publish(requestedIds);
    }

    private Map<Long, InterfaceJobEntity> loadProcessJobs(List<Long> requestedIds) {
        Map<Long, InterfaceJobEntity> jobsById = interfaceJobRepository.findAllByInterfaceJobIdIn(requestedIds)
            .stream()
            .collect(Collectors.toMap(InterfaceJobEntity::getInterfaceJobId, job -> job));

        if (jobsById.size() != requestedIds.stream().distinct().count()) {
            Set<Long> missingIds = requestedIds.stream()
                .filter(id -> !jobsById.containsKey(id))
                .collect(Collectors.toSet());
            throw new EntityNotFoundException("Interface jobs not found: " + missingIds);
        }
        return jobsById;
    }

    private void validateRequestedBusinessUnits(List<InterfaceJobsProcessItem> requestedJobs,
                                                Map<Long, InterfaceJobEntity> jobsById) {
        for (InterfaceJobsProcessItem requestedJob : requestedJobs) {
            Short persistedBusinessUnitId = jobsById.get(requestedJob.getInterfaceJobId())
                .getBusinessUnit().getBusinessUnitId();
            if (!Objects.equals(persistedBusinessUnitId, requestedJob.getBusinessUnitId())) {
                throw new IllegalArgumentException("Business unit does not match interface job "
                    + requestedJob.getInterfaceJobId());
            }
        }
    }

    private void validateProcessPermissions(List<InterfaceJobsProcessItem> requestedJobs) {
        List<Short> requestedBusinessUnitIds = requestedJobs.stream()
            .map(InterfaceJobsProcessItem::getBusinessUnitId)
            .distinct()
            .toList();
        List<Short> permittedBusinessUnitIds = userStateService.getPermittedBusinessUnitIds(
            requestedBusinessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        requestedBusinessUnitIds.stream()
            .filter(id -> !permittedBusinessUnitIds.contains(id))
            .findFirst()
            .ifPresent(id -> {
                throw new PermissionNotAllowedException(id, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
            });
    }

    private void validateProcessStatuses(List<Long> requestedIds, Map<Long, InterfaceJobEntity> jobsById) {
        for (Long requestedId : requestedIds) {
            InterfaceJobEntity job = jobsById.get(requestedId);
            if (job.getStatus() != InterfaceJobStatus.CREATED && job.getStatus() != InterfaceJobStatus.FAILED) {
                throw new ResourceConflictException(
                    "InterfaceJob", job.getInterfaceJobId(), "Interface job must be CREATED or FAILED", null);
            }
        }
    }

    private void updateForProcessing(InterfaceJobsProcessItem requestedJob, InterfaceJobEntity job,
                                     LocalDateTime startedDateTime) {
        job.setStatus(InterfaceJobStatus.PROCESSING);
        job.setStartedDateTime(startedDateTime);
        job.getInterfaceFiles().forEach(file -> file.setOverrideInhibits(requestedJob.getOverrideInhibits()));
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InterfaceJobSearchCriteria implements ToJsonString {

        @JsonProperty("business_unit_ids")
        @Getter(AccessLevel.NONE)
        private List<Short> businessUnitIds;

        private List<Short> permittedBusinessUnitIds;

        @JsonProperty("statuses")
        private List<String> statuses;

        @JsonProperty("completed_date_from")
        private LocalDateTime completedDateFrom;

        @JsonProperty("completed_date_to")
        private LocalDateTime completedDateTo;

        @JsonProperty("interface_name")
        private String interfaceName;

        public List<Short> getPermittedBusinessUnitIds(UserStateService userStateService) {
            permittedBusinessUnitIds = userStateService.getPermittedBusinessUnitIds(
                businessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
            return permittedBusinessUnitIds;
        }

        public List<InterfaceJobStatus> getInterfaceJobStatuses() {
            return statuses == null ? null : statuses.stream()
                .map(InterfaceJobStatus::valueOf)
                .toList();
        }
    }
}
