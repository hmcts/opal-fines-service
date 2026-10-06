package uk.gov.hmcts.opal.service.interfacejob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerInterfaceFiles;
import uk.gov.hmcts.opal.service.interfacejob.json.InterfaceJobJsonMapper;

@ExtendWith(MockitoExtension.class)
class InterfaceJobProcessorServiceTest {

    private static final Long INTERFACE_JOB_ID = 123L;
    private static final Short BUSINESS_UNIT_ID = 77;
    private static final Long INTERFACE_FILE_ID = 456L;
    private static final Long TRANSFORMED_JSON_ID = 5512L;
    private static final String EXTRACT_JSON = "{\n"
        + "  \"file_name\": \"a121_00350005_300000.dat\""
        + "}";
    private static final String RECORDS_JSON = "{\n"
        + "  destination_sort_code: \"10-10-10\"\n"
        + "}";

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private InterfaceJobJsonMapper jsonMapper;

    @Mock
    private FileHandlerInterfaceFiles fileHandlerInterfaceFiles;

    @InjectMocks
    private InterfaceJobProcessorService interfaceJobProcessorService;

    @SneakyThrows
    void setupJsonMocks() {
        Resource fileContent = mock(Resource.class);
        when(fileContent.getContentAsString(Charset.defaultCharset())).thenReturn(EXTRACT_JSON);
        when(fileHandlerInterfaceFiles.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, TRANSFORMED_JSON_ID))
            .thenReturn(fileContent);
        when(jsonMapper.toRecordsJson(EXTRACT_JSON)).thenReturn(RECORDS_JSON);
    }

    @Test
    void processPaymentsInJob_isTransactional() throws NoSuchMethodException {
        // Arrange
        Method method = InterfaceJobProcessorService.class.getMethod("processPaymentsInJob", Long.class);

        // Act
        Transactional transactional = method.getAnnotation(Transactional.class);

        // Assert
        assertThat(transactional).isNotNull();
    }

    @Test
    void processPaymentsInJob_returnsTillIdWhenStoredProcedureSucceeds() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        setupJsonMocks();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs", RECORDS_JSON)).thenReturn(456L);

        // Act
        Optional<Long> tillId = interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID);

        // Assert
        assertThat(tillId).contains(456L);
        verify(interfaceJobRepository).findById(INTERFACE_JOB_ID);
        verify(interfaceJobRepository).processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs", RECORDS_JSON);
    }

    @Test
    void processPaymentsInJob_returnsEmptyWhenStoredProcedureReturnsNull() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        setupJsonMocks();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs", RECORDS_JSON)).thenReturn(null);

        // Act
        Optional<Long> tillId = interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID);

        // Assert
        assertThat(tillId).isEmpty();
    }

    @Test
    void processPaymentsInJob_whenJobCannotBeFound_throwsException() {
        // Arrange
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to process interface job 123")
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Interface job not found with id: 123");
    }

    @Test
    void processPaymentsInJob_transformedJsonIdNotPresent_throwsException() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob(false);
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act / Assert
        assertThatThrownBy(() -> interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to process interface job 123")
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Interface job 123 does not have associated transformedJsonId");
    }

    @Test
    void processPaymentsInJob_whenRepositoryThrows_wrapsException() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        setupJsonMocks();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(eq(INTERFACE_JOB_ID), eq(BUSINESS_UNIT_ID),
            eq("interface-jobs"), eq("interface-jobs"), eq(RECORDS_JSON)))
            .thenThrow(new RuntimeException("db down"));

        // Act / Assert
        assertThatThrownBy(() -> interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to process interface job 123")
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("db down");
    }

    private static InterfaceJobEntity interfaceJob() {
        return interfaceJob(true);
    }

    private static InterfaceJobEntity interfaceJob(boolean withTransformedJsonId) {
        InterfaceFileEntity interfaceFile = InterfaceFileEntity.builder()
            .interfaceFileId(INTERFACE_FILE_ID)
            .build();

        if (withTransformedJsonId) {
            interfaceFile.setTransformedJsonId(TRANSFORMED_JSON_ID);
        }

        return InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .businessUnit(BusinessUnitEntity.builder()
                .businessUnitId(BUSINESS_UNIT_ID)
                .build())
            .interfaceFiles(List.of(interfaceFile))
            .build();
    }
}
