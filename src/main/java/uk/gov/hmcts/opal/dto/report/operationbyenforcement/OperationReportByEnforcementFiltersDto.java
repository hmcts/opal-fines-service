package uk.gov.hmcts.opal.dto.report.operationbyenforcement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountStatusReportFilterType;
import uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationReportByEnforcementFiltersDto {

    private ReportType reportType;
    private List<Long> businessUnitIds;
    private ReportEnforcementMode reportEnforcementMode;
    private String enforcementAction;
    private LocalDate enforcementDateFrom;
    private LocalDate enforcementDateTo;
    private LocalDate lastActionDateFrom;
    private LocalDate lastActionDateTo;
    private LocalDate regfDateFrom;
    private LocalDate regfDateTo;
    private Boolean includeAdult;
    private Boolean includeYouth;
    private Boolean includeCompany;
    private Boolean onlyAccountsWithParentGuardian;
    private CollectionOrderReportFilterType collectionOrderChoice;
    private AccountStatusReportFilterType accountStatus;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private Boolean firstPaymentOrPayByInNext7Days;
    private String lowerNameRange;
    private String upperNameRange;

}