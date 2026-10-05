package uk.gov.hmcts.opal.service.interfacejob.json;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.interfacejob.json.fileHandler.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

@Component
public class InterfaceJobJsonMapper {
    private final ObjectMapper objectMapper;
    private final InterfaceJobRecordMapper recordMapper;

    public InterfaceJobJsonMapper(
        @Qualifier("snakeCaseObjectMapper") ObjectMapper objectMapper, InterfaceJobRecordMapper recordMapper) {
        this.objectMapper = objectMapper;
        this.recordMapper = recordMapper;
    }

    public String toRecordsJson(String extractJson) {
        InterfaceFileCommonDataExtract extract = objectMapper
            .readValue(extractJson, InterfaceFileCommonDataExtract.class);
        InterfaceJobRecord[] records = recordMapper.mapToRecords(extract);
        return objectMapper.writeValueAsString(records);
    }
}
