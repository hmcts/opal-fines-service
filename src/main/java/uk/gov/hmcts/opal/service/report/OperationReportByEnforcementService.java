package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_NUMBER;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_ENFORCEMENT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;

@Service
@RequiredArgsConstructor
public class OperationReportByEnforcementService implements ReportInterface {

    private final DefendantAccountRepository defendantAccountRepository;
    private final ReportResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ReportId getReportId() {
        return OP_ENFORCEMENT;
    }

    @Override
    public ReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {
        String parameters = reportInstance.getReportParameters();
        ReportFiltersDto filters = objectMapper.readValue(parameters, ReportFiltersDto.class);
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        Sort sort = Sort.by(Sort.Direction.ASC, ACCOUNT_NUMBER);
        List<DefendantAccountEntity> accounts = defendantAccountRepository.findAll(spec, sort);
        return resultMapper.map(accounts);
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, ReportDataInterface reportData,
        FileType fileType) {
        return new byte[0];
    }
}