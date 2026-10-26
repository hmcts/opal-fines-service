package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.common.exception.DownstreamServiceUnavailableException;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerAPIService;
import uk.gov.hmcts.opal.service.filehandler.InterfaceBusinessUnitResolver;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.AccountNumberTranslationService;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.OriginatorDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.PaymentType;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.Transaction;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ExtendWith(MockitoExtension.class)
class InterfaceFileProcessorServiceTest {

    private static final Long SOURCE_ID = 987L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 12, 10, 0);
    private static final SystemUserEnum SYSTEM_USER = SystemUserEnum.OPAL_SYSTEM_USER;

    @Mock
    private InterfaceFileRepository interfaceFileRepository;

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private SimpleTransactionStatus transactionStatus;

    @Mock
    private FileHandlerAPIService fileHandlerAPIService;

    @Mock
    private InterfaceBusinessUnitResolver businessUnitResolver;

    @Mock
    private AccountNumberTranslationService accountNumberTranslationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private InterfaceFileProcessorService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneOffset.UTC);
        transactionStatus = new SimpleTransactionStatus();
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        service = new InterfaceFileProcessorService(clock, objectMapper, interfaceFileRepository,
            interfaceJobRepository, new TransactionTemplate(transactionManager), fileHandlerAPIService,
            businessUnitResolver, accountNumberTranslationService);
    }

    @ParameterizedTest
    @EnumSource(PaymentType.class)
    @DisplayName("Processes a source JSON file and saves the transformed file and linked rows")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_uploadsTransformedJsonAndSavesLinkedRows(PaymentType paymentType) throws Exception {
        stubSuccessfulProcessing(paymentType);
        when(fileHandlerAPIService.addInterfaceFile(eq(SYSTEM_USER), any(), any()))
            .thenReturn(InterfaceFileObject.builder().interfaceFileId(988L).build());

        service.process(SOURCE_ID);

        assertTransformedFileUploaded(paymentType);
        assertLinkedRowsSaved(paymentType);
    }

    @Test
    @DisplayName("Claims the source before transforming or uploading it")
    void process_claimsSourceBeforeUpload() {
        stubSuccessfulProcessing(PaymentType.CASH);
        when(fileHandlerAPIService.addInterfaceFile(eq(SYSTEM_USER), any(), any()))
            .thenReturn(InterfaceFileObject.builder().interfaceFileId(988L).build());

        service.process(SOURCE_ID);

        var order = inOrder(interfaceJobRepository, accountNumberTranslationService, fileHandlerAPIService);
        order.verify(interfaceJobRepository).saveAndFlush(any());
        order.verify(accountNumberTranslationService).process(any(), any(), any());
        order.verify(fileHandlerAPIService).addInterfaceFile(eq(SYSTEM_USER), any(), any());
    }

    @Test
    @DisplayName("Skips a source JSON file that has already been processed")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_skipsSourceThatWasAlreadyProcessed() {
        when(interfaceFileRepository.findBySourceJsonId(SOURCE_ID)).thenReturn(Optional.of(new InterfaceFileEntity()));

        service.process(SOURCE_ID);

        verifyNoInteractions(fileHandlerAPIService, interfaceJobRepository, transactionManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"type", "domain", "target", "status"})
    @DisplayName("Rejects invalid source file metadata before upload")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_rejectsInvalidSourceBeforeUpload(String invalidField) {
        InterfaceFileObject.Builder source = sourceFile();

        switch (invalidField) {
            case "type" -> source.type(InterfaceFileTypeEnum.SOURCE);
            case "domain" -> source.domain(InterfaceFileObject.DomainEnum.FILE_HANDLER);
            case "target" -> source.target(InterfaceFileEnum.NATWEST);
            case "status" -> source.status(StatusEnum.FAILED);
            default -> throw new IllegalArgumentException(invalidField);
        }
        when(fileHandlerAPIService.getInterfaceFile(SYSTEM_USER, SOURCE_ID))
            .thenReturn(source.build());

        assertThatThrownBy(() -> service.process(SOURCE_ID)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(interfaceJobRepository);
        verify(transactionManager).rollback(transactionStatus);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Rejects an upload response without a transformed file ID")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_rejectsUploadWithoutFileId(boolean missingResponse) {
        stubSuccessfulProcessing(PaymentType.CASH);
        when(fileHandlerAPIService.addInterfaceFile(eq(SYSTEM_USER), any(), any()))
            .thenReturn(missingResponse ? null : new InterfaceFileObject());

        assertThatThrownBy(() -> service.process(SOURCE_ID)).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("transformed interface file ID");

        verify(transactionManager).rollback(transactionStatus);
    }

    @ParameterizedTest
    @ValueSource(strings = {"metadata", "content", "upload"})
    @DisplayName("Propagates the File Handler exception when processing fails")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_propagatesFileHandlerFailure(String failingCall) {
        DownstreamServiceUnavailableException failure = new DownstreamServiceUnavailableException(
            "File Handler returned HTTP 503", new IllegalStateException("Unexpected response"));

        switch (failingCall) {
            case "metadata" -> when(fileHandlerAPIService.getInterfaceFile(SYSTEM_USER, SOURCE_ID))
                .thenThrow(failure);
            case "content" -> {
                when(fileHandlerAPIService.getInterfaceFile(SYSTEM_USER, SOURCE_ID)).thenReturn(sourceFile().build());
                when(fileHandlerAPIService.getInterfaceFileContent(SYSTEM_USER, SOURCE_ID)).thenThrow(failure);
            }
            case "upload" -> {
                stubSuccessfulProcessing(PaymentType.CASH);
                when(fileHandlerAPIService.addInterfaceFile(eq(SYSTEM_USER), any(), any())).thenThrow(failure);
            }
            default -> throw new IllegalArgumentException(failingCall);
        }

        assertThatThrownBy(() -> service.process(SOURCE_ID)).isSameAs(failure);
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    @DisplayName("A concurrent claim stops processing before translation or upload")
    void process_skipsConcurrentClaim() {
        stubSuccessfulProcessing(PaymentType.CASH);
        when(interfaceFileRepository.findBySourceJsonId(SOURCE_ID))
            .thenReturn(Optional.empty()).thenReturn(Optional.of(new InterfaceFileEntity()));
        when(interfaceJobRepository.saveAndFlush(any()))
            .thenThrow(new DataIntegrityViolationException("Duplicate source JSON"));

        service.process(SOURCE_ID);

        verifyNoInteractions(accountNumberTranslationService);
        verify(fileHandlerAPIService, never()).addInterfaceFile(any(), any(), any());
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    @DisplayName("An unrelated integrity failure is propagated")
    void process_propagatesIntegrityFailure() {
        stubSuccessfulProcessing(PaymentType.CASH);
        DataIntegrityViolationException failure = new DataIntegrityViolationException("Invalid job");
        when(interfaceJobRepository.saveAndFlush(any())).thenThrow(failure);

        assertThatThrownBy(() -> service.process(SOURCE_ID)).isSameAs(failure);

        verifyNoInteractions(accountNumberTranslationService);
        verify(fileHandlerAPIService, never()).addInterfaceFile(any(), any(), any());
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    @DisplayName("A commit failure is propagated after uploading")
    void process_propagatesCommitFailure() {
        stubSuccessfulProcessing(PaymentType.CASH);
        when(fileHandlerAPIService.addInterfaceFile(eq(SYSTEM_USER), any(), any()))
            .thenReturn(InterfaceFileObject.builder().interfaceFileId(988L).build());
        IllegalStateException failure = new IllegalStateException("Commit failed");
        doThrow(failure).when(transactionManager).commit(transactionStatus);

        assertThatThrownBy(() -> service.process(SOURCE_ID)).isSameAs(failure);
    }

    private void assertTransformedFileUploaded(PaymentType paymentType) throws Exception {
        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        ArgumentCaptor<AddInterfaceFileRequestMetadata> metadataCaptor =
            ArgumentCaptor.forClass(AddInterfaceFileRequestMetadata.class);

        verify(fileHandlerAPIService).addInterfaceFile(eq(SYSTEM_USER), fileCaptor.capture(), metadataCaptor.capture());

        AddInterfaceFileRequestMetadata metadata = metadataCaptor.getValue();

        assertThat(metadata.getRelatedInterfaceFileId()).isEqualTo(SOURCE_ID);
        assertThat(metadata.getSource()).isEqualTo(InterfaceFileEnum.BTECKOH);
        assertThat(metadata.getTarget()).isEqualTo(InterfaceFileEnum.OPAL);
        assertThat(metadata.getType()).isEqualTo(InterfaceFileTypeEnum.TRANSFORMED_JSON);
        assertThat(metadata.getDomain().toString()).isEqualTo("FINES");
        assertThat(metadata.getPaymentType().toString()).isEqualTo(paymentType.name());
        assertThat(metadata.getBusinessUnitCode()).isEqualTo("BC12");
        assertThat(metadata.getShouldPreProcessFile()).isFalse();
        assertThat(fileCaptor.getValue().getOriginalFilename()).isEqualTo("test_123");
        assertThat(fileCaptor.getValue().getContentType()).isEqualTo("application/json");
        JsonNode uploadedContent = objectMapper.readTree(fileCaptor.getValue().getBytes());
        assertThat(uploadedContent.path("transactions").size()).isEqualTo(5);
        assertThat(uploadedContent.path("total_amount").asLong()).isEqualTo(5_000L);
    }

    private void assertLinkedRowsSaved(PaymentType paymentType) {
        ArgumentCaptor<InterfaceJobEntity> jobCaptor = ArgumentCaptor.forClass(InterfaceJobEntity.class);

        verify(interfaceJobRepository).saveAndFlush(jobCaptor.capture());
        verify(interfaceFileRepository, never()).save(any());

        InterfaceJobEntity job = jobCaptor.getValue();
        assertThat(job.getInterfaceFiles()).hasSize(1);
        InterfaceFileEntity finesFile = job.getInterfaceFiles().getFirst();

        assertThat(job.getBusinessUnit().getBusinessUnitCode()).isEqualTo("BC12");
        assertThat(job.getInterfaceName()).isEqualTo(paymentType == PaymentType.CASH
            ? "PAYMENTS_IN" : "PRESENTED_CHEQUES");
        assertThat(job.getStatus()).isEqualTo(InterfaceJobStatus.CREATED);
        assertThat(job.getCreatedDateTime()).isEqualTo(NOW);
        assertThat(job.getStartedDateTime()).isNull();
        assertThat(job.getCompletedDateTime()).isNull();
        assertThat(finesFile.getInterfaceJob()).isSameAs(job);
        assertThat(finesFile.getFileName()).isEqualTo("test_123");
        assertThat(finesFile.getSource()).isEqualTo("BTECKOH");
        assertThat(finesFile.getCreatedDateTime()).isEqualTo(NOW);
        assertThat(finesFile.getSourceJsonId()).isEqualTo(SOURCE_ID);
        assertThat(finesFile.getTransformedJsonId()).isEqualTo(988L);
        assertThat(finesFile.getRecordCount()).isEqualTo((short) 5);
        assertThat(finesFile.getTotalAmount()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    private void stubSuccessfulProcessing(PaymentType paymentType) {
        InterfaceFileCommonDataExtract sourceData = sourceData(paymentType);
        TransformedInterfaceFileData transformedData = new TransformedInterfaceFileData(sourceData);
        sourceData.getTransactions().forEach(transformedData::addTransaction);
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId((short) 6460).businessUnitCode("BC12").build();
        when(fileHandlerAPIService.getInterfaceFile(SYSTEM_USER, SOURCE_ID)).thenReturn(sourceFile().build());
        when(fileHandlerAPIService.getInterfaceFileContent(SYSTEM_USER, SOURCE_ID))
            .thenReturn(new ByteArrayResource(objectMapper.writeValueAsBytes(sourceData)));
        when(businessUnitResolver.resolve(any())).thenReturn(businessUnit);
        lenient().when(accountNumberTranslationService.process(
            nullable(InterfaceFileEntity.class), any(), eq(businessUnit))).thenReturn(transformedData);
    }

    private InterfaceFileObject.Builder sourceFile() {
        return InterfaceFileObject.builder()
            .interfaceFileId(SOURCE_ID)
            .fileName("test_123")
            .checksum("CS123")
            .filestoreUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .source(InterfaceFileEnum.BTECKOH)
            .target(InterfaceFileEnum.OPAL)
            .type(InterfaceFileTypeEnum.SOURCE_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .status(StatusEnum.SUCCESS);
    }

    private InterfaceFileCommonDataExtract sourceData(PaymentType paymentType) {
        String[] codes = paymentType == PaymentType.CASH
            ? new String[] {"00", "15", "68", "93", "99"}
            : new String[] {"11", "11", "11", "11", "11"};

        List<Transaction> transactions = java.util.Arrays.stream(codes)
            .map(code -> Transaction.builder()
                .transactionCode(code)
                .originatorDetails(OriginatorDetails.builder().accountReference("unmatched").name("Payer").build())
                .amount(1_000L)
                .build())
            .toList();

        return InterfaceFileCommonDataExtract.builder()
            .fileName("test_123")
            .destinationDetails(DestinationDetails.builder()
                .bankDetails(BankDetails.builder().sortCode("123456").accountNumber("01234567").build())
                .build())
            .paymentType(paymentType)
            .transactions(transactions)
            .build();
    }
}
