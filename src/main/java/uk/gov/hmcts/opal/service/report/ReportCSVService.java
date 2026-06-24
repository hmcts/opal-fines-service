package uk.gov.hmcts.opal.service.report;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;

/**
 * Service to convert JSON report data to CSV byte data.
 * TODO this must be wired up to the endpoint created by PO-2253
 */
@Service
@AllArgsConstructor
@Slf4j(topic = "opal.ReportCSVService")
public class ReportCSVService {
    private final ReportCSVMapperRegistry reportCSVMapperRegistry;

    /**
     * Creates a Comma Separated Values (CSV) for the given data. The reportDataInterface will have a specific
     * ReportCSVMapper.
     * @param <T> The implementation of the reportDataInterface
     * @param reportDataInterface the report data to be used to generate the CSV, this will likely be retrieved from
     *                            the (azure) report blob store, so will have been already generated
     * @return the CSV byte array
     * @param <T> The implementation of the reportDataInterface
     * @param reportDataInterface the report data to be used to generate the CSV, this will likely be retrieved from
     *                            the (azure) report blob store, so will have been already generated
     * @return the CSV byte array
     */
    public <T extends ReportDataInterface> byte[] convertReportDtoToCSV(T reportDataInterface) {
        @SuppressWarnings("unchecked")
        ReportCSVMapper<T> reportCSVMapper = (ReportCSVMapper<T>) reportCSVMapperRegistry
            .get(reportDataInterface.getClass());
        if (reportCSVMapper == null) {
            throw new UnprocessableException("Report cannot be converted to CSV format.");
        }
        String csv = reportCSVMapper.reportToCSVString(reportDataInterface);
        return csv.getBytes();
    }
}
