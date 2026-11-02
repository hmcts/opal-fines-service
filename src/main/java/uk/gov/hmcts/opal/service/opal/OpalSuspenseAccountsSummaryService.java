package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemSummaryMapper;
import uk.gov.hmcts.opal.repository.SuspenseItemSummaryRepository;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;

@Service
@Slf4j(topic = "opal.OpalSuspenseAccountsSummaryService")
@RequiredArgsConstructor
public class OpalSuspenseAccountsSummaryService implements SuspenseAccountsSummaryServiceInterface {

    private final SuspenseItemSummaryRepository suspenseItemSummaryRepository;
    private final SuspenseItemSummaryMapper suspenseItemSummaryMapper;

    @Override
    public SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Short businessUnitId, Long suspenseItemNumber) {

        SuspenseItemSummaryEntity suspenseItemSummary = suspenseItemSummaryRepository
            .findByBusinessUnitIdAndSuspenseItemNumber(suspenseItemNumber, businessUnitId);

        return suspenseItemSummaryMapper.toSuspenseItemSummaryCommonStrict(suspenseItemSummary);
    }

}
