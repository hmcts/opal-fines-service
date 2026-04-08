package uk.gov.hmcts.opal.service.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFiltersDto {

    private List<Long> businessUnitIds;
    private String enforcementMode;
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
    private String collectionOrderChoice;
    private String accountStatus;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private Boolean firstPaymentOrPaybyInNext7Days;
    private String lowerNameRange;
    private String upperNameRange;

}