package uk.gov.hmcts.opal.service.report;

import java.math.BigDecimal;
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

    private final DefendantAccountRepository defendantAccountRepository;
    private final ReportResultMapper resultMapper;

    @Override
    public ReportDataInterface generateReportData(ReportInstanceEntity reportInstance) {
        DocContext context = JsonPathUtil.createDocContext(reportInstance.getReportParameters(), "");
        String reportType = safeReadString(context, "$.Report_types", "Detailed");

        // Build filters from JSON
        ReportFiltersDto filters = buildFiltersFromContext(context);

        // Build specification and fetch accounts
        Specification<DefendantAccountEntity> spec = ReportSpecs.build(filters);

        // Deterministic ordering
        Sort sort = Sort.by(Sort.Direction.ASC, "accountNumber");

        List<DefendantAccountEntity> accounts = defendantAccountRepository.findAll(spec, sort);

        return resultMapper.map(accounts);
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, ReportDataInterface reportData,
        FileType fileType) {
        return new byte[0];
    }

    private ReportFiltersDto buildFiltersFromContext(DocContext context) {
        ReportFiltersDto.ReportFiltersDtoBuilder builder = ReportFiltersDto.builder();

        try {
            List<Number> buIds = context.read("$.business_unit_ids");
            if (buIds != null) {
                builder.businessUnitIds(buIds.stream().map(Number::longValue).collect(Collectors.toList()));
            }
        } catch (Exception ignored) {
            ignored.getMessage();
        }

        String enforcementMode = safeReadString(context, "$.enforcementMode", null);
        if (enforcementMode == null) {
            enforcementMode = "ALL";
        }
        builder.enforcementMode(enforcementMode);

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

        builder.collectionOrderChoice(safeReadString(context, "$.collectionOrderChoice", "all"));
        builder.accountStatus(safeReadString(context, "$.accountStatus", "all"));

        builder.minBalance(safeReadBigDecimal(context, "$.minBalance"));
        builder.maxBalance(safeReadBigDecimal(context, "$.maxBalance"));

        builder.firstPaymentOrPaybyInNext7Days(safeReadBoolean(context, "$.firstPaymentOrPaybyInNext7Days",
            Boolean.FALSE));

        builder.lowerNameRange(safeReadString(context, "$.lowerNameRange", null));
        builder.upperNameRange(safeReadString(context, "$.upperNameRange", null));

        return builder.build();
    }


    private static String safeReadString(DocContext ctx, String path, String def) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return def;
            }
            return String.valueOf(v);
        } catch (Exception e) {
            return def;
        }
    }

    private static java.time.LocalDate safeReadLocalDate(DocContext ctx, String path) {
        try {
            String s = safeReadString(ctx, path, null);
            if (s == null || s.isBlank()) {
                return null;
            }
            return java.time.LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean safeReadBoolean(DocContext ctx, String path, Boolean def) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return def;
            }
            if (v instanceof Boolean) {
                return (Boolean) v;
            }
            String s = String.valueOf(v).trim();
            if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y")
                || s.equalsIgnoreCase("yes")) {
                return Boolean.TRUE;
            }
            if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n")
                || s.equalsIgnoreCase("no")) {
                return Boolean.FALSE;
            }
            return def;
        } catch (Exception e) {
            return def;
        }
    }

    private static java.math.BigDecimal safeReadBigDecimal(DocContext ctx, String path) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return null;
            }
            if (v instanceof Number) {
                return new BigDecimal(String.valueOf(v));
            }
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}