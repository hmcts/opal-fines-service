package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerAPIService;
import uk.gov.hmcts.opal.service.filehandler.InterfaceBusinessUnitResolver;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.AccountNumberTranslationService;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.PaymentType.CASH;

import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;

@ExtendWith(MockitoExtension.class)
class InterfaceFileProcessorServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InterfaceFileRepository interfaceFileRepository;

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private FileHandlerAPIService fileHandlerAPIService;

    @Mock
    private InterfaceBusinessUnitResolver businessUnitResolver;

    @Mock
    private AccountNumberTranslationService accountNumberTranslationService;

    @InjectMocks
    private InterfaceFileProcessorService interfaceFileProcessorService;

    @Test
    void whenSourceJsonAlreadyProcessed_returnsImmediately() {

        when(interfaceFileRepository.findBySourceJsonId(123L))
            .thenReturn(Optional.of(mock(InterfaceFileEntity.class)));

        interfaceFileProcessorService.process(123L);

        verify(interfaceFileRepository).findBySourceJsonId(123L);

        verifyNoInteractions(
            fileHandlerAPIService,
            businessUnitResolver,
            accountNumberTranslationService
        );
    }

    @Test
    void whenSourceJsonIsNotSourceJson_throwsIllegalArgumentException() {

        InterfaceFileObject interfaceFile = new InterfaceFileObject()
            .type(InterfaceFileTypeEnum.TRANSFORMED_JSON);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(interfaceFile);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> interfaceFileProcessorService.process(123L));

        assertEquals("Interface file is not a successful FINES SOURCE_JSON targeted at OPAL",
            exception.getMessage());

    }

    @Test
    void whenSourceJsonCannotBeRead_throwsIllegalArgumentException() throws Exception {

        Resource resource = mock(Resource.class);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,
            123L)).thenReturn(validSourceJson());

        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,
            123L)).thenReturn(resource);

        when(resource.getContentAsByteArray()).thenThrow(new IOException());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> interfaceFileProcessorService.process(123L));

        assertEquals("Unable to read source JSON content", exception.getMessage());
    }

    @Test
    void whenSourceJsonDomainIsNull_throwsIllegalArgumentException() {

        InterfaceFileObject interfaceFile = validSourceJson();
        interfaceFile.setDomain(null);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,
            123L)).thenReturn(interfaceFile);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> interfaceFileProcessorService.process(123L));

        assertEquals("Interface file is not a successful FINES SOURCE_JSON targeted at OPAL",
            exception.getMessage());

    }

    @Test
    void whenSourceJsonStatusIsNull_throwsIllegalArgumentException() {

        InterfaceFileObject interfaceFile = validSourceJson();
        interfaceFile.setStatus(null);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,
            123L)).thenReturn(interfaceFile);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> interfaceFileProcessorService.process(123L));

        assertEquals("Interface file is not a successful FINES SOURCE_JSON targeted at OPAL",
            exception.getMessage());

    }


    @Test
    void shouldThrowWhenSourceJsonNotSuccessful() {

        InterfaceFileObject file = new InterfaceFileObject()
            .interfaceFileId(1L)
            .type(InterfaceFileTypeEnum.SOURCE_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .target(InterfaceFileEnum.OPAL)
            .status(StatusEnum.FAILED);

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,1L))
            .thenReturn(file);

        assertThatThrownBy(() -> interfaceFileProcessorService.process(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(
                "successful FINES SOURCE_JSON targeted at OPAL");

    }


    @Test
    void shouldThrowWhenTransformedJsonCannotBeSerialised() {

        RuntimeException exception = new RuntimeException("boom");

        when(objectMapper.writeValueAsBytes(any())).thenThrow(exception);

        assertThatThrownBy(() ->
            ReflectionTestUtils.invokeMethod(
                interfaceFileProcessorService,
                "serialiseTransformedData",
                mock(TransformedInterfaceFileData.class)))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("boom");

    }

    @Test
    void shouldThrowWhenFileHandlerReturnsNullTransformedFile() throws Exception {

        Resource resource = mock(Resource.class);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);

        TransformedInterfaceFileData transformed = mock(TransformedInterfaceFileData.class);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(validSourceJson());

        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(resource);

        when(resource.getContentAsByteArray()).thenReturn("{}".getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(InterfaceFileCommonDataExtract.class)))
            .thenReturn(extract);

        when(businessUnitResolver.resolve(extract)).thenReturn(businessUnit);

        when(accountNumberTranslationService.process(null, extract,
            businessUnit)).thenReturn(transformed);

        when(transformed.getFileName()).thenReturn("output.json");

        when(transformed.getPaymentType()).thenReturn(CASH);
        when(objectMapper.writeValueAsBytes(transformed)).thenReturn("{}".getBytes());

        when(fileHandlerAPIService.addInterfaceFile(any(), any(), any())).thenReturn(null);

        assertThatThrownBy(() ->
            interfaceFileProcessorService.process(123L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(
                "File Handler did not return a transformed interface file ID");

    }

    @Test
    void shouldPersistPaymentsInJobForCashPayment() throws Exception {

        Resource resource = mock(Resource.class);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);

        when(businessUnit.getBusinessUnitCode()).thenReturn("BU01");

        TransformedInterfaceFileData transformed = mock(TransformedInterfaceFileData.class);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(validSourceJson());

        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(resource);

        when(resource.getContentAsByteArray()).thenReturn("{}".getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(InterfaceFileCommonDataExtract.class)))
            .thenReturn(extract);

        when(businessUnitResolver.resolve(extract)).thenReturn(businessUnit);

        when(accountNumberTranslationService.process(null, extract, businessUnit))
            .thenReturn(transformed);

        when(transformed.getFileName()).thenReturn("transformed.json");

        // THIS WAS MISSING
        when(transformed.getPaymentType()).thenReturn(CASH);

        when(objectMapper.writeValueAsBytes(transformed)).thenReturn("{}".getBytes());

        InterfaceFileObject transformedFile = new InterfaceFileObject().interfaceFileId(999L);

        when(fileHandlerAPIService.addInterfaceFile(any(), any(), any())).thenReturn(transformedFile);

        interfaceFileProcessorService.process(123L);

        verify(fileHandlerAPIService).addInterfaceFile(any(), any(), any());

    }

    @Test
    void shouldThrowWhenTransformedFileIdIsNull() throws Exception {

        Resource resource = mock(Resource.class);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);

        TransformedInterfaceFileData transformed = mock(TransformedInterfaceFileData.class);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(validSourceJson());

        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(resource);

        when(resource.getContentAsByteArray()).thenReturn("{}".getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(InterfaceFileCommonDataExtract.class)))
            .thenReturn(extract);

        when(businessUnitResolver.resolve(extract)).thenReturn(businessUnit);

        when(accountNumberTranslationService.process(null, extract, businessUnit))
            .thenReturn(transformed);

        when(transformed.getFileName()).thenReturn("output.json");

        when(transformed.getPaymentType()).thenReturn(CASH);

        when(objectMapper.writeValueAsBytes(transformed)).thenReturn("{}".getBytes());

        InterfaceFileObject transformedFile = new InterfaceFileObject();

        when(fileHandlerAPIService.addInterfaceFile(any(), any(), any())).thenReturn(transformedFile);

        assertThatThrownBy(() ->
            interfaceFileProcessorService.process(123L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(
                "File Handler did not return a transformed interface file ID");

    }

    @Test
    void shouldProcessSourceJsonSuccessfully() throws Exception {

        Resource resource = mock(Resource.class);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);

        TransformedInterfaceFileData transformed = mock(TransformedInterfaceFileData.class);

        InterfaceFileObject transformedFile = new InterfaceFileObject().interfaceFileId(999L);

        when(interfaceFileRepository.findBySourceJsonId(123L)).thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(validSourceJson());

        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,123L))
            .thenReturn(resource);

        when(resource.getContentAsByteArray()).thenReturn("{}".getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(InterfaceFileCommonDataExtract.class)))
            .thenReturn(extract);

        when(businessUnitResolver.resolve(extract)).thenReturn(businessUnit);

        when(businessUnit.getBusinessUnitCode()).thenReturn("BU01");

        when(accountNumberTranslationService.process(null, extract, businessUnit))
            .thenReturn(transformed);

        when(transformed.getFileName()).thenReturn("transformed.json");

        when(transformed.getPaymentType()).thenReturn(CASH);

        when(objectMapper.writeValueAsBytes(transformed)).thenReturn("{}".getBytes());

        when(fileHandlerAPIService.addInterfaceFile(any(), any(), any()))
            .thenReturn(transformedFile);

        interfaceFileProcessorService.process(123L);

        verify(fileHandlerAPIService).addInterfaceFile(any(), any(), any());

        verify(transactionTemplate).executeWithoutResult(any());

    }

    private InterfaceFileObject validSourceJson() {

        return new InterfaceFileObject()
            .interfaceFileId(123L)
            .fileName("source.json")
            .type(InterfaceFileTypeEnum.SOURCE_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .target(InterfaceFileEnum.OPAL)
            .status(StatusEnum.SUCCESS);
    }

}
