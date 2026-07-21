package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity_;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.jpa.InterfaceJobSpecs;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@Slf4j(topic = "opal.InterfaceJobService")
@RequiredArgsConstructor
public class InterfaceJobService {

    private static final Sort SUMMARY_SORT = Sort.by(
        Sort.Order.asc(InterfaceJobEntity_.BUSINESS_UNIT + "." + BusinessUnitEntity_.BUSINESS_UNIT_NAME),
        Sort.Order.desc(InterfaceJobEntity_.CREATED_DATE_TIME));

    private final InterfaceJobRepository interfaceJobRepository;

    private final InterfaceJobMapper interfaceJobMapper;

    private final UserStateService userStateService;

    private final InterfaceJobSpecs specs = new InterfaceJobSpecs();

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

    public void deleteInterfaceJobs(List<Long> interfaceJobIds) {
        if (interfaceJobIds.isEmpty()) {
            log.warn("TEST ENDPOINT: No interface job ids supplied for deletion");
            return;
        }

        log.warn("DESTRUCTIVE OPERATION: Deleting interface jobs with ids: {}", interfaceJobIds);

        interfaceJobRepository.deleteAllById(interfaceJobIds);
    }

    private List<InterfaceJobsSummaryItem> toResponses(InterfaceJobEntity interfaceJob) {
        return interfaceJob.getInterfaceFiles().stream()
            .map(interfaceFile -> interfaceJobMapper.toSummaryResponse(interfaceJob, interfaceFile))
            .toList();
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
