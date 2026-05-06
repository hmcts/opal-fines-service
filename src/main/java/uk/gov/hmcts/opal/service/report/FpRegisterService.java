package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.FP_REGISTER;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

@Service
public class FpRegisterService implements ReportInterface {

    @Override
    public ReportId getReportId() {
        return FP_REGISTER;
    }

    @Override
    public FpRegisterData generateReportData(ReportInstanceEntity reportInstance) {
        return new FpRegisterData();
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, ReportDataInterface reportData,
        FileType fileType) {
        return new byte[0];
    }

    public static class FpRegisterData implements ReportDataInterface {

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
