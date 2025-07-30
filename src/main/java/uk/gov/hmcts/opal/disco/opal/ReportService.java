package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;
import uk.gov.hmcts.opal.disco.ReportServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportService")
public class ReportService implements ReportServiceInterface {

    private final ReportRepository reportRepository;

    private final ReportSpecs specs = new ReportSpecs();

    @Override
    public ReportEntity getReport(long reportId) {
        return reportRepository.getReferenceById(reportId);
    }

    @Override
    public List<ReportEntity> searchReports(ReportSearchDto criteria) {
        Page<ReportEntity> page = reportRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
