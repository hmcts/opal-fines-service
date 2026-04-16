package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.OP_ENFORCEMENT;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadBigDecimal;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadBoolean;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLocalDate;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportSpecs;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class OperationReportByEnforcementService implements ReportInterface {

    public static final String ALL = "ALL";
    private final DefendantAccountRepository defendantAccountRepository;
    private final ReportResultMapper resultMapper;

    @Override
    public ReportId getReportId() {
        return OP_ENFORCEMENT;
    }

    @Override
    public ReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {
        DocContext context = JsonPathUtil.createDocContext(reportInstance.getReportParameters(), "");
        ReportType reportType = ReportType.fromLabel(safeReadString(context, "$.Report_types", "Detailed"));
        ReportFiltersDto filters = buildFiltersFromContext(context, reportType);
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);
        Sort sort = Sort.by(Sort.Direction.ASC, "accountNumber");
        List<DefendantAccountEntity> accounts = defendantAccountRepository.findAll(spec, sort);
        return resultMapper.map(accounts);
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, ReportDataInterface reportData,
        FileType fileType) {
        return new byte[0];
    }

    private ReportFiltersDto buildFiltersFromContext(DocContext context, ReportType reportType) {
        ReportFiltersDto.ReportFiltersDtoBuilder builder = ReportFiltersDto.builder();
        builder.reportType(reportType);
        try {
            List<Number> buIds = context.read("$.business_unit_ids");
            if (buIds != null) {
                builder.businessUnitIds(buIds.stream().map(Number::longValue).collect(Collectors.toList()));
            }
        } catch (Exception ignored) {
            ignored.getMessage();
        }
        builder.reportEnforcementMode(parseEnforcementMode(
            safeReadString(context, "$.enforcementMode", ALL)
        ));
        builder.enforcementDateFrom(safeReadLocalDate(context, "$.enforcementDateFrom"));
        builder.enforcementDateTo(safeReadLocalDate(context, "$.enforcementDateTo"));

        builder.lastActionDateFrom(safeReadLocalDate(context, "$.lastActionDateFrom"));
        builder.lastActionDateTo(safeReadLocalDate(context, "$.lastActionDateTo"));

        builder.regfDateFrom(safeReadLocalDate(context, "$.regfDateFrom"));
        builder.regfDateTo(safeReadLocalDate(context, "$.regfDateTo"));

        builder.includeAdult(safeReadBoolean(context, "$.includeAdult", Boolean.FALSE));
        builder.includeYouth(safeReadBoolean(context, "$.includeYouth", Boolean.FALSE));
        builder.includeCompany(safeReadBoolean(context, "$.includeCompany", Boolean.FALSE));

        builder.onlyAccountsWithParentGuardian(safeReadBoolean(context, "$.onlyAccountsWithParentGuardian",
            Boolean.FALSE));

        builder.collectionOrderChoice(safeReadString(context, "$.collectionOrderChoice", ALL));
        builder.accountStatus(safeReadString(context, "$.accountStatus", ALL));

        builder.minBalance(safeReadBigDecimal(context, "$.minBalance"));
        builder.maxBalance(safeReadBigDecimal(context, "$.maxBalance"));

        builder.firstPaymentOrPaybyInNext7Days(safeReadBoolean(context, "$.firstPaymentOrPaybyInNext7Days",
            Boolean.FALSE));

        builder.lowerNameRange(safeReadString(context, "$.lowerNameRange", null));
        builder.upperNameRange(safeReadString(context, "$.upperNameRange", null));

        return builder.build();
    }

    private ReportEnforcementMode parseEnforcementMode(String value) {
        if (value == null) {
            return ReportEnforcementMode.ALL;
        }

        try {
            return ReportEnforcementMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReportEnforcementMode.ALL;
        }
    }
}