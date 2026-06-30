package uk.gov.hmcts.opal.service.report;

import java.util.List;
import java.util.Map;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;

public final class GetReportInstanceContentTestData {

    private GetReportInstanceContentTestData() {
    }

    public static ReportEntity createReportEntity(String reportId, FinesPermission permission) {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportId(reportId);
        reportEntity.setPermission(permission);
        reportEntity.setSupportedFileTypes(List.of(
            SupportedFileType.CSV,
            SupportedFileType.PDF,
            SupportedFileType.JSON,
            SupportedFileType.XML
        ));
        return reportEntity;
    }

    public static ReportInstanceEntity createReportInstanceEntity(
        String reportId,
        FinesPermission permission,
        List<Integer> businessUnits
    ) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReport(createReportEntity(reportId, permission));
        reportInstance.setBusinessUnit(businessUnits);
        return reportInstance;
    }

    public static StoredReportContent createStoredReportContent(Map<String, Object> reportData) {
        return StoredReportContent.builder()
            .reportData(reportData)
            .build();
    }

    public static TestReportData createTestReportData() {
        return new TestReportData();
    }

    public static final class TestReportData implements ReportDataInterface {

        @Override
        public long getNumberOfRecords() {
            return 0;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return new ReportMetaData(List.of());
        }
    }
}
