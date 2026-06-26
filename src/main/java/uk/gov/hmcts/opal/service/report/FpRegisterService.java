package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.ReportId.FP_REGISTER;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

@Service
public class FpRegisterService implements ReportInterface<FpRegisterService.FpRegisterData> {

    @Override
    public ReportId getReportId() {
        return FP_REGISTER;
    }

    @Override
    public FpRegisterData generateReportData(ReportInstanceEntity reportInstance) {
        return new FpRegisterData();
    }

    @Override
    public Class<? extends FpRegisterData> getStoredReportDataClass(ReportInstanceEntity reportInstance) {
        return FpRegisterData.class;
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, FpRegisterData reportData,
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
