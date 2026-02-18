package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.service.report.ReportMetaData;

public interface ReportDataInterface {

    long getNumberOfRecords();

    ReportMetaData getReportMetaData();
}

