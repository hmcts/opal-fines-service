package uk.gov.hmcts.opal.service.report;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;

@Service
@AllArgsConstructor
@Slf4j(topic = "opal.ReportCSVService")
public class ReportCSVService {
    private Map<Class<? extends ReportDataInterface>, ReportCSVMapper<? extends ReportDataInterface>>
        reportToCSVStringMapperMap;

    /**
     * Creates a Comma Separated Values (CSV) for the given data. The reportDataInterface will have a specific
     * ReportCSVMapper.
     * @param reportDataInterface the report data to be used to generate the CSV, this will likely be retrieved from
     *                            the (azure) report blob store
     * @return the CSV byte array
     * @param <T> The implementation of the reportDataInterface
     */
    public <T extends ReportDataInterface> byte[] covertReportDtoToCSV(T reportDataInterface) {
        //do like mapper to string then bytes from string?

        //pick from the reportToCSVStringMapperMap using the class of the input reportDataInterface
        //mapper makes csv string, this converts it to the byte[] specified in the ticket
        @SuppressWarnings("unchecked")
        ReportCSVMapper<T> reportCSVMapper = (ReportCSVMapper<T>) reportToCSVStringMapperMap
            .get(reportDataInterface.getClass());
        if (reportCSVMapper == null) {
            throw new IllegalArgumentException("Report cannot be converted to CSV format.");
        }
        return reportCSVMapper.reportToCSVString(reportDataInterface).getBytes();
    }
}
