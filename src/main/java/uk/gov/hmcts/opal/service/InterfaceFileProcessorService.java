package uk.gov.hmcts.opal.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.DomainEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.PaymentTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerAPIService;
import uk.gov.hmcts.opal.service.filehandler.InterfaceBusinessUnitResolver;
import uk.gov.hmcts.opal.service.filehandler.TransformedJsonMultipartFile;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.AccountNumberTranslationService;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceFileProcessorService {

    private static final String PAYMENTS_IN = "PAYMENTS_IN";
    private static final String PRESENTED_CHEQUES = "PRESENTED_CHEQUES";

    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final InterfaceFileRepository interfaceFileRepository;
    private final InterfaceJobRepository interfaceJobRepository;
    private final TransactionTemplate transactionTemplate;
    private final FileHandlerAPIService fileHandlerAPIService;
    private final InterfaceBusinessUnitResolver businessUnitResolver;
    private final AccountNumberTranslationService accountNumberTranslationService;

    public void process(Long sourceJsonId) {
        if (interfaceFileRepository.findBySourceJsonId(sourceJsonId).isPresent()) {
            log.info("Source JSON interfaceFileId={} has already been processed", sourceJsonId);
            return;
        }

        try {
            Long transformedJsonId = transactionTemplate.execute(status -> processSourceJson(sourceJsonId));
            log.info("Processed source JSON interfaceFileId={} into transformed interfaceFileId={}",
                sourceJsonId, transformedJsonId);
        } catch (DataIntegrityViolationException exception) {
            if (interfaceFileRepository.findBySourceJsonId(sourceJsonId).isEmpty()) {
                throw exception;
            }

            log.info("Source JSON interfaceFileId={} has already been processed", sourceJsonId);
        }
    }

    private Long processSourceJson(Long sourceJsonId) {
        InterfaceFileObject sourceJson = getSourceJson(sourceJsonId);
        InterfaceFileCommonDataExtract sourceData = deserialiseSourceJson(
            fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, sourceJsonId));
        BusinessUnitEntity businessUnit = businessUnitResolver.resolve(sourceData);
        InterfaceFileEntity file = claimSourceJson(sourceJson, sourceData, businessUnit);

        TransformedInterfaceFileData transformedData = accountNumberTranslationService.process(
            file, sourceData, businessUnit);
        InterfaceFileObject transformedJson = uploadTransformedJson(sourceJson, transformedData, businessUnit);

        file.setRecordCount((short) transformedData.getTransactions().size());
        file.setTotalAmount(BigDecimal.valueOf(transformedData.getTotalAmount()));
        file.setTransformedJsonId(transformedJson.getInterfaceFileId());

        return transformedJson.getInterfaceFileId();
    }

    private InterfaceFileEntity claimSourceJson(InterfaceFileObject sourceJson,
        InterfaceFileCommonDataExtract sourceData, BusinessUnitEntity businessUnit) {

        String interfaceName = switch (sourceData.getPaymentType()) {
            case CASH -> PAYMENTS_IN;
            case CHEQUE -> PRESENTED_CHEQUES;
        };

        InterfaceJobEntity job = InterfaceJobEntity.builder()
            .businessUnit(businessUnit)
            .interfaceName(interfaceName)
            .status(InterfaceJobStatus.CREATED)
            .createdDateTime(LocalDateTime.now(clock))
            .build();

        InterfaceFileEntity file = InterfaceFileEntity.builder()
            .interfaceJob(job)
            .fileName(sourceJson.getFileName())
            .createdDateTime(LocalDateTime.now(clock))
            .source(sourceJson.getSource().toString())
            .sourceJsonId(sourceJson.getInterfaceFileId())
            .build();

        job.getInterfaceFiles().add(file);
        interfaceJobRepository.saveAndFlush(job);

        return file;
    }

    private InterfaceFileObject getSourceJson(Long sourceJsonId) {
        InterfaceFileObject sourceJson = fileHandlerAPIService.getInterfaceFile(
            SystemUserEnum.OPAL_SYSTEM_USER, sourceJsonId);

        if (sourceJson.getType() != InterfaceFileTypeEnum.SOURCE_JSON
            || sourceJson.getDomain() != InterfaceFileObject.DomainEnum.FINES
            || sourceJson.getTarget() != InterfaceFileEnum.OPAL
            || sourceJson.getStatus() != StatusEnum.SUCCESS) {
            throw new IllegalArgumentException(
                "Interface file is not a successful FINES SOURCE_JSON targeted at OPAL");
        }

        return sourceJson;
    }

    private InterfaceFileCommonDataExtract deserialiseSourceJson(Resource content) {
        try {
            return objectMapper.readValue(content.getContentAsByteArray(), InterfaceFileCommonDataExtract.class);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read source JSON content", exception);
        }
    }

    private InterfaceFileObject uploadTransformedJson(
        InterfaceFileObject sourceJson,
        TransformedInterfaceFileData transformedData,
        BusinessUnitEntity businessUnit
    ) {
        byte[] content = serialiseTransformedData(transformedData);

        MultipartFile file = new TransformedJsonMultipartFile(transformedData.getFileName(), content);
        AddInterfaceFileRequestMetadata metadata = AddInterfaceFileRequestMetadata.builder()
            .relatedInterfaceFileId(sourceJson.getInterfaceFileId())
            .shouldPreProcessFile(false)
            .source(sourceJson.getSource())
            .target(InterfaceFileEnum.OPAL)
            .type(InterfaceFileTypeEnum.TRANSFORMED_JSON)
            .domain(DomainEnum.FINES)
            .paymentType(PaymentTypeEnum.fromValue(transformedData.getPaymentType().name()))
            .fileName(transformedData.getFileName())
            .businessUnitCode(businessUnit.getBusinessUnitCode())
            .build();

        InterfaceFileObject transformedJson = fileHandlerAPIService.addInterfaceFile(
            SystemUserEnum.OPAL_SYSTEM_USER, file, metadata);

        if (transformedJson == null || transformedJson.getInterfaceFileId() == null) {
            throw new IllegalStateException("File Handler did not return a transformed interface file ID");
        }
        return transformedJson;
    }

    private byte[] serialiseTransformedData(TransformedInterfaceFileData transformedData) {
        try {
            return objectMapper.writeValueAsBytes(transformedData);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Unable to serialise transformed interface file", exception);
        }
    }
}
