package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.OP_ENFORCEMENT;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.mapper.report.OperationReportByEnforcementResultMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;

@Service
@RequiredArgsConstructor
public class OperationReportByEnforcementService implements ReportInterface {

    private final DefendantAccountRepository defendantAccountRepository;
    private final EnforcementRepository enforcementRepository;
    private final OperationReportByEnforcementResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ReportId getReportId() {
        return OP_ENFORCEMENT;
    }

    @Override
    public ReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {

        ReportFiltersDto filters = readFilters(reportInstance);
        ReportEnforcementMode mode = filters.getReportEnforcementMode();

        if (mode == ReportEnforcementMode.NOT_UNDER_ENFORCEMENT) {

            Specification<DefendantAccountEntity> spec =
                ReportSpecs.accountFiltersSpec(filters)
                    .and(EnforcementReportSpecs.notUnderEnforcement());

            List<DefendantAccountEntity> accounts =
                defendantAccountRepository.findAll(spec, Sort.by("accountNumber"));

            return resultMapper.map(accounts);
        }

        Specification<EnforcementEntity> spec =
            EnforcementReportSpecs.build(filters);
        List<EnforcementEntity> enforcements =
            enforcementRepository.findAll(spec);

        List<DefendantAccountEntity> accounts = enforcements.stream()
            .map(EnforcementEntity::getDefendantAccount)
            .distinct()
            .sorted(Comparator.comparing(DefendantAccountEntity::getAccountNumber))
            .toList();

        return resultMapper.map(accounts);
    }

    private ReportFiltersDto readFilters(ReportInstanceEntity reportInstance) {
        try {
            return objectMapper.readValue(
                reportInstance.getReportParameters(),
                ReportFiltersDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse report filters", e);
        }
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, ReportDataInterface reportData,
        FileType fileType) {
        return new byte[0];
    }

}