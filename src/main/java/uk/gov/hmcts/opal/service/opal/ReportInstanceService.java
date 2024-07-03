package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs;
import uk.gov.hmcts.opal.service.ReportInstanceServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportInstanceService")
public class ReportInstanceService implements ReportInstanceServiceInterface {

    private final ReportInstanceRepository reportInstanceRepository;

    private final ReportInstanceSpecs specs = new ReportInstanceSpecs();

    @Override
    public ReportInstanceEntity getReportInstance(long reportInstanceId) {
        return reportInstanceRepository.getReferenceById(reportInstanceId);
    }

    @Override
    public List<ReportInstanceEntity> searchReportInstances(ReportInstanceSearchDto criteria) {
        Page<ReportInstanceEntity> page = reportInstanceRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
