package uk.gov.hmcts.opal.service.opal;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.VersionedResponse;
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
    @Transactional(readOnly = true)
    public VersionedResponse<SuspenseItemSummaryCommonStrict> getSuspenseAccountsSummary(Short businessUnitId,
        BigInteger suspenseItemNumber) {

        SuspenseItemSummaryEntity suspenseItemSummary = suspenseItemSummaryRepository
            .findByBusinessUnitIdAndSuspenseItemNumber(businessUnitId, suspenseItemNumber);

        if (suspenseItemSummary == null) {
            return null;
        }

        return new VersionedResponse<>(
            suspenseItemSummaryMapper.toSuspenseItemSummaryCommonStrict(suspenseItemSummary),
            suspenseItemSummary.getVersionNumber());
    }

}
