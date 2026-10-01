package uk.gov.hmcts.opal.service.interfacejob.json;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.interfacejob.json.fileHandler.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

@Component
@RequiredArgsConstructor
public class InterfaceJobJsonMapper {
    private final ObjectMapper objectMapper;
    private final InterfaceJobRecordMapper recordMapper;

    public String toRecordsJson(String extractJson) {
        InterfaceFileCommonDataExtract extract = objectMapper.readValue(extractJson, InterfaceFileCommonDataExtract.class);
        InterfaceJobRecord[] records = recordMapper.mapToRecords(extract);
        return objectMapper.writeValueAsString(records);
    }
}
