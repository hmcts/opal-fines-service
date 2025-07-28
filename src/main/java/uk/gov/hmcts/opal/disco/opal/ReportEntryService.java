package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportEntrySpecs;
import uk.gov.hmcts.opal.disco.ReportEntryServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("reportEntryService")
public class ReportEntryService implements ReportEntryServiceInterface {

    private final ReportEntryRepository reportEntryRepository;

    private final ReportEntrySpecs specs = new ReportEntrySpecs();

    @Override
    public ReportEntryEntity getReportEntry(long reportEntryId) {
        return reportEntryRepository.getReferenceById(reportEntryId);
    }

    @Override
    public List<ReportEntryEntity> searchReportEntries(ReportEntrySearchDto criteria) {
        Page<ReportEntryEntity> page = reportEntryRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
