package uk.gov.hmcts.opal.dto.report.operation;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.dto.AccountStatusReportFilterType;
import uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType;
import uk.gov.hmcts.opal.service.report.ReportType;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationReportFiltersDto {

    private ReportType reportType;
    private List<Long> businessUnitIds;
    private String lowerNameRange;
    private String upperNameRange;
    private Boolean includeAdult;
    private Boolean includeYouth;
    private Boolean includeCompany;
    private Boolean onlyAccountsWithParentGuardian;
    private CollectionOrderReportFilterType collectionOrderChoice;
    private AccountStatusReportFilterType accountStatus;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private Boolean firstPaymentOrPayByInNext7Days;

}