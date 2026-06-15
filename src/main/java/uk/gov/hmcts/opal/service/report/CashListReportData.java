package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashListReportData implements ReportDataInterface {

    private TillDetails tillDetails;
    private List<CashListEntry> entries;
    private BigDecimal total;

    @Override
    @JsonIgnore
    public long getNumberOfRecords() {
        return entries == null ? 0 : entries.size();
    }

    @Override
    @JsonIgnore
    public ReportMetaData getReportMetaData() {
        return new ReportMetaData(List.of());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TillDetails {
        private Long tillId;
        private Short tillNumber;
        private String ownedBy;
        private Short businessUnitId;
        private String businessUnitName;
        private String businessUnitCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashListEntry {
        private int entry;
        private String type;
        private String suspense;
        private String accountNumber;
        private String name;
        private String nameAdditionalInformation;
        private String paymentMethod;
        private BigDecimal amount;
    }
}
