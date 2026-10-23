package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
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

        when(interfaceFileRepository.findBySourceJsonId(123L))
            .thenReturn(Optional.empty());

        when(fileHandlerAPIService.getInterfaceFile(
            SystemUserEnum.OPAL_SYSTEM_USER,
            123L
        )).thenReturn(interfaceFile);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interfaceFileProcessorService.process(123L)
        );

        assertEquals(
            "Interface file is not a successful FINES SOURCE_JSON targeted at OPAL",
            exception.getMessage()
        );
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
