package uk.gov.hmcts.opal.service.interfacejob.json;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.interfacejob.json.fileHandler.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

@ExtendWith(MockitoExtension.class)
public class InterfaceJobJsonMapperTest {

    @Mock
    private InterfaceJobRecordMapper jobRecordMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InterfaceJobJsonMapper mapper;

    @Test void toRecordsJson_correctlyOrchestratesCalls() throws JsonProcessingException {
        // Arrange
        String extractJson = "{\"foo\":\"bar\"}";
        String recordsJson = "[{\"amount_pence\"=1599}]";
        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);
        when(objectMapper.readValue(extractJson, InterfaceFileCommonDataExtract.class)).thenReturn(extract);
        InterfaceJobRecord[] records = mock(InterfaceJobRecord[].class);
        when(jobRecordMapper.mapToRecords(extract)).thenReturn(records);
        when(objectMapper.writeValueAsString(records)).thenReturn(recordsJson);


        // Act
        String result = mapper.toRecordsJson(extractJson);

        // Assert
        assertThat(result).isEqualTo(recordsJson);
    }


}
