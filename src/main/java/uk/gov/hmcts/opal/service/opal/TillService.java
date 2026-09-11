package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;
import uk.gov.hmcts.opal.entity.TillSummaryEntity_;
import uk.gov.hmcts.opal.generated.model.TillsItem;
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.mapper.TillMapper;
import uk.gov.hmcts.opal.repository.TillSummaryRepository;
import uk.gov.hmcts.opal.repository.jpa.TillSummarySpecs;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class TillService {

    private static final Sort TILL_SORT = Sort.by(
        Sort.Order.asc(TillSummaryEntity_.BUSINESS_UNIT_NAME),
        Sort.Order.desc(TillSummaryEntity_.DATE_PROCESSED));

    private final TillSummaryRepository tillSummaryRepository;

    private final TillMapper tillMapper;

    private final UserStateService userStateService;

    private final TillSummarySpecs specs = new TillSummarySpecs();

    @Transactional(readOnly = true)
    public TillsResponse getTills(TillSearchCriteria searchCriteria) {
        if (searchCriteria.getPermittedBusinessUnitIds(userStateService).isEmpty()) {
            return TillsResponse.builder().tills(List.of()).build();
        }

        Page<TillSummaryEntity> tills = tillSummaryRepository.findBy(
            specs.findBySearchCriteria(searchCriteria),
            ffq -> ffq
                .sortBy(TILL_SORT)
                .page(Pageable.unpaged()));

        List<TillsItem> responseItems = tills.getContent().stream()
            .map(tillMapper::toResponse)
            .toList();

        return TillsResponse.builder().tills(responseItems).build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TillSearchCriteria implements ToJsonString {

        @JsonProperty("business_unit_ids")
        @Getter(AccessLevel.NONE)
        private List<Short> businessUnitIds;

        private List<Short> permittedBusinessUnitIds;

        @JsonProperty("statuses")
        private List<String> statuses;

        @JsonProperty("auto_payments")
        private Boolean autoPayments;

        public List<Short> getPermittedBusinessUnitIds(UserStateService userStateService) {
            permittedBusinessUnitIds = userStateService.getPermittedBusinessUnitIds(
                businessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
            return permittedBusinessUnitIds;
        }

        public List<TillStatusEnum> getTillStatuses() {
            return statuses == null ? null : statuses.stream()
                .map(TillSearchCriteria::toTillStatus)
                .toList();
        }

        private static TillStatusEnum toTillStatus(String status) {
            return Arrays.stream(TillStatusEnum.values())
                .filter(tillStatus -> tillStatus.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown till status: " + status));
        }
    }
}
